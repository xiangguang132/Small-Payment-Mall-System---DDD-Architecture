package cn.bugstack.domain.order.service;

import cn.bugstack.domain.order.adapter.port.IProductPort;
import cn.bugstack.domain.order.adapter.repository.IOrderLockRepository;
import cn.bugstack.domain.order.adapter.repository.IOrderRepository;
import cn.bugstack.domain.order.model.aggregate.CreateOrderAggregate;
import cn.bugstack.domain.order.model.entity.*;
import cn.bugstack.domain.order.model.valobj.OrderStatusVO;
import cn.bugstack.domain.groupbuy.model.entity.CouponEntity;
import cn.bugstack.domain.groupbuy.repository.ICouponRepository;
import cn.bugstack.domain.groupbuy.repository.IUserCouponRepository;
import cn.bugstack.domain.groupbuy.service.trial.rule.coupon.ICouponCalculateService;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public abstract class AbstractOrderService implements IOrderService {

    protected final IOrderRepository orderRepository;
    protected final IOrderLockRepository orderLockRepository;
    protected final IProductPort productPort;
    protected final ICouponRepository couponRepository;
    protected final IUserCouponRepository userCouponRepository;

    public AbstractOrderService(IOrderRepository orderRepository, IOrderLockRepository orderLockRepository,
                                IProductPort productPort, ICouponRepository couponRepository,
                                IUserCouponRepository userCouponRepository) {
        this.orderRepository = orderRepository;
        this.orderLockRepository = orderLockRepository;
        this.productPort = productPort;
        this.couponRepository = couponRepository;
        this.userCouponRepository = userCouponRepository;
    }

    @Override
    public PayOrderEntity createOrder(String userId, String productId, String lockId) throws Exception {
        // 1. 查询锁单记录（锁单仅校验有效性，商品与用户由创单请求提供）
        OrderLockEntity lockEntity = orderLockRepository.queryLockByLockId(lockId);
        if (lockEntity == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "锁单不存在");
        }
        if (!"LOCKED".equals(lockEntity.getLockStatus())) {
            throw new AppException(ResponseCode.UN_ERROR, "锁单状态异常，当前状态：" + lockEntity.getLockStatus());
        }
        if (lockEntity.isExpired()) {
            orderLockRepository.updateLockStatus(lockId, "EXPIRED");
            throw new AppException(ResponseCode.UN_ERROR, "锁单已过期，请重新锁单");
        }

        // 2. 通过 productId 查询商品信息
        ProductEntity productEntity = productPort.queryProductByProductId(productId);
        if (productEntity == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "商品不存在");
        }

        // 3. 幂等复用：同用户同商品已有待支付订单时，直接复用旧支付单，避免重复创单
        OrderEntity unpaidOrder = orderRepository.queryUnPayOrder(
                ShopCartEntity.builder().userId(userId).productId(productId).build());
        if (unpaidOrder != null && unpaidOrder.getOrderStatus() == OrderStatusVO.PAY_WAIT) {
            PayOrderEntity existPayOrder = orderRepository.queryPayOrderByOutTradeNo(unpaidOrder.getOutTradeNo());
            if (existPayOrder != null && existPayOrder.getPayUrl() != null && !existPayOrder.getPayUrl().isEmpty()) {
                // 当前锁单未被消费，置为过期防止悬挂
                orderLockRepository.updateLockStatus(lockId, "EXPIRED");
                log.info("幂等复用未支付订单 userId:{} productId:{} outTradeNo:{} lockId:{}",
                        userId, productId, unpaidOrder.getOutTradeNo(), lockId);
                return existPayOrder;
            }
        }

        // 4. 基于商品信息构建订单
        OrderEntity orderEntity = CreateOrderAggregate.buildOrderEntity(
                productId, productEntity.getProductName()
        );

        // 5. 优惠券计算：从锁单记录读取 couponIds，计算折扣价
        BigDecimal originalPrice = productEntity.getPrice();
        BigDecimal payAmount = originalPrice;
        String couponIdsJson = lockEntity.getCouponIds();

        if (couponIdsJson != null && !couponIdsJson.isEmpty() && !"[]".equals(couponIdsJson)) {
            List<String> couponIds = JSON.parseArray(couponIdsJson, String.class);
            if (couponIds != null && !couponIds.isEmpty()) {
                payAmount = calculateCouponDiscount(originalPrice, couponIds);
                log.info("直购优惠券计算 userId:{} 原价:{} 券后价:{} couponIds:{}", userId, originalPrice, payAmount, couponIds);
            }
        }

        // 6. 保存订单（含券信息）
        CreateOrderAggregate orderAggregate = CreateOrderAggregate.builder()
                .userId(userId)
                .productEntity(productEntity)
                .orderEntity(orderEntity)
                .payAmount(payAmount)
                .originalAmount(originalPrice)
                .couponIds(couponIdsJson)
                .build();

        orderRepository.doSaveOrder(orderAggregate);

        // 7. 冻结优惠券：status 0→4，防止同一张券被多个未支付订单占用
        if (couponIdsJson != null && !couponIdsJson.isEmpty() && !"[]".equals(couponIdsJson)) {
            List<String> couponIds = JSON.parseArray(couponIdsJson, String.class);
            if (couponIds != null && !couponIds.isEmpty()) {
                int frozen = userCouponRepository.freezeUserCoupons(userId, couponIds, orderEntity.getOutTradeNo(), LocalDateTime.now());
                log.info("直购冻结优惠券 userId:{} couponIds:{} 冻结数量:{} outTradeNo:{}", userId, couponIds, frozen, orderEntity.getOutTradeNo());
            }
        }

        // 8. 创建支付单（折扣后金额）
        PayOrderEntity payOrderEntity = this.doPrepayOrder(
                userId, productId, productEntity.getProductName(),
                orderEntity.getOutTradeNo(), payAmount, originalPrice, couponIdsJson
        );

        // 9. 确认锁单：回写 orderId
        orderLockRepository.updateOrderId(lockId, orderEntity.getOutTradeNo());

        return payOrderEntity;
    }

    /**
     * 计算优惠券折扣：取所有券中实付价最低的（对单商品原价与购物车聚合金额同样适用）
     */
    protected BigDecimal calculateCouponDiscount(BigDecimal originalPrice, List<String> couponIds) {
        BigDecimal bestPrice = originalPrice;
        for (String couponId : couponIds) {
            CouponEntity coupon = couponRepository.queryCouponByCouponId(couponId);
            if (coupon == null || coupon.getStatus() != 1) continue;
            // 找到对应的计算策略
            ICouponCalculateService calculator = getCouponCalculateService(coupon.getCouponType());
            if (calculator == null) continue;
            BigDecimal result = calculator.calculate(originalPrice, coupon);
            if (result != null && result.compareTo(bestPrice) < 0) {
                bestPrice = result;
            }
        }
        return bestPrice;
    }

    /**
     * 获取优惠券计算策略（由子类注入）
     */
    protected abstract ICouponCalculateService getCouponCalculateService(String couponType);

    protected abstract void doSaveOrder(CreateOrderAggregate orderAggregate);

    protected abstract PayOrderEntity doPrepayOrder(String userId, String productId, String productName,
                                                     String outTradeNo, BigDecimal totalAmount,
                                                     BigDecimal originalAmount, String couponIds) throws AlipayApiException;

}
