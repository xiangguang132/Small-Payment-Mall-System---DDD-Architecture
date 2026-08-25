package cn.bugstack.domain.order.service;

import cn.bugstack.domain.order.adapter.port.IProductPort;
import cn.bugstack.domain.order.adapter.repository.IOrderLockRepository;
import cn.bugstack.domain.order.adapter.repository.IOrderRepository;
import cn.bugstack.domain.order.model.aggregate.CreateOrderAggregate;
import cn.bugstack.domain.order.model.entity.*;
import cn.bugstack.domain.order.model.valobj.OrderStatusVO;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import com.alipay.api.AlipayApiException;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
public abstract class AbstractOrderService implements IOrderService {

    protected final IOrderRepository orderRepository;
    protected final IOrderLockRepository orderLockRepository;
    protected final IProductPort productPort;

    public AbstractOrderService(IOrderRepository orderRepository, IOrderLockRepository orderLockRepository, IProductPort productPort) {
        this.orderRepository = orderRepository;
        this.orderLockRepository = orderLockRepository;
        this.productPort = productPort;
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
        CreateOrderAggregate orderAggregate = CreateOrderAggregate.builder()
                .userId(userId)
                .productEntity(productEntity)
                .orderEntity(orderEntity)
                .build();

        // 5. 保存订单
        orderRepository.doSaveOrder(orderAggregate);

        // 6. 创建支付单
        PayOrderEntity payOrderEntity = this.doPrepayOrder(
                userId,
                productId,
                productEntity.getProductName(),
                orderEntity.getOutTradeNo(),
                productEntity.getPrice()
        );

        // 7. 确认锁单：回写 orderId
        orderLockRepository.updateOrderId(lockId, orderEntity.getOutTradeNo());

        return payOrderEntity;
    }

    protected abstract void doSaveOrder(CreateOrderAggregate orderAggregate);

    protected abstract PayOrderEntity doPrepayOrder(String userId, String productId, String productName, String outTradeNo, BigDecimal totalAmount) throws AlipayApiException;

}
