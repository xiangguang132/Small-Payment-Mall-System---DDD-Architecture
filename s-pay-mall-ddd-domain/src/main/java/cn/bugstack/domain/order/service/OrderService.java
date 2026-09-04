package cn.bugstack.domain.order.service;

import cn.bugstack.domain.order.adapter.port.IProductPort;
import cn.bugstack.domain.order.adapter.repository.IOrderLockRepository;
import cn.bugstack.domain.groupbuy.repository.ICouponRepository;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyOrderRepository;
import cn.bugstack.domain.order.adapter.repository.IOrderRepository;
import cn.bugstack.domain.order.model.aggregate.CreateOrderAggregate;
import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.model.valobj.OrderStatusVO;
import cn.bugstack.domain.groupbuy.service.trial.rule.coupon.ICouponCalculateService;
import cn.bugstack.domain.payment.adapter.port.IAlipayPort;
import cn.bugstack.domain.payment.adapter.port.IAlipayRefundPort;
import cn.bugstack.types.enums.OrderTypeEnum;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 该部分并不专注于实现业务，而是将有操作数据库或者http外部数据的
 * 进行下沉
 * 下沉到 Repository 接口层
 */

@Slf4j
@Service
public class OrderService extends AbstractOrderService{

    @org.springframework.beans.factory.annotation.Value("${alipay.notify_url}")
    private String notifyUrl;
    @Value("${alipay.return_url}")
    private String returnUrl;
    @Resource
    private AlipayClient alipayClient;
    @Resource
    private IAlipayRefundPort alipayRefundPort;
    @Autowired
    private IAlipayPort alipayPort;

    private final IGroupBuyOrderRepository groupBuyOrderRepository;
    private final ICouponRepository couponRepository;
    private final Map<String, ICouponCalculateService> couponCalculateServices;

    public OrderService(IOrderRepository orderRepository, IOrderLockRepository orderLockRepository,
                        IProductPort productPort, IGroupBuyOrderRepository groupBuyOrderRepository,
                        ICouponRepository couponRepository,
                        Map<String, ICouponCalculateService> couponCalculateServices) {
        super(orderRepository, orderLockRepository, productPort, couponRepository);
        this.groupBuyOrderRepository = groupBuyOrderRepository;
        this.couponRepository = couponRepository;
        this.couponCalculateServices = couponCalculateServices;
    }

    @Override
    protected ICouponCalculateService getCouponCalculateService(String couponType) {
        return couponCalculateServices.get(couponType);
    }

    /**
     * 保存订单
     * @param orderAggregate
     */
    @Override
    protected void doSaveOrder(CreateOrderAggregate orderAggregate) {
        orderRepository.doSaveOrder(orderAggregate);
    }

    /**
     * 调用支付宝接口生成预支付订单，并返回支付表单和更新订单状态
     * @param userId
     * @param productId
     * @param productName
     * @param outTradeNo
     * @param totalAmount
     * @return
     * @throws AlipayApiException
     */
    @Override
    protected PayOrderEntity doPrepayOrder(String userId, String productId, String productName,
                                            String outTradeNo, BigDecimal totalAmount,
                                            BigDecimal originalAmount, String couponIds) throws AlipayApiException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(returnUrl);
        request.setNotifyUrl(notifyUrl);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("total_amount", formatAmount(totalAmount));
        bizContent.put("subject", productName);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());

        String form = alipayClient.pageExecute(request).getBody();

        PayOrderEntity payOrderEntity = new PayOrderEntity();
        payOrderEntity.setUserId(userId);
        payOrderEntity.setProductId(productId);
        payOrderEntity.setProductName(productName);
        payOrderEntity.setOutTradeNo(outTradeNo);
        payOrderEntity.setOrderTime(LocalDateTime.now());
        payOrderEntity.setTotalAmount(totalAmount);
        payOrderEntity.setOriginalAmount(originalAmount);
        payOrderEntity.setOrderType(OrderTypeEnum.DIRECT);
        payOrderEntity.setOrderStatus(OrderStatusVO.PAY_WAIT);
        payOrderEntity.setCouponIds(couponIds);
        payOrderEntity.setPayUrl(form);

        orderRepository.updateOrderPayInfo(payOrderEntity);

        return payOrderEntity;
    }

    /**
     * 创建拼团支付单（GROUP_BUY 类型）
     * 与 doPrepayOrder 的区别：orderType=GROUP_BUY，金额为拼团实付价（而非商品原价）
     * @param userId 用户ID
     * @param productId 商品ID
     * @param productName 商品名称
     * @param outTradeNo 商户订单号，需与 group_buy_order.out_trade_no 一致，回调结算按此反查
     * @param totalAmount 拼团实付金额 payAmount
     * @return 拼团支付单（含支付宝支付表单 payUrl）
     */
    public PayOrderEntity createGroupBuyPayOrder(String userId, String productId,
                                                 String productName, String outTradeNo,
                                                 BigDecimal totalAmount) throws AlipayApiException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(returnUrl);
        request.setNotifyUrl(notifyUrl);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("total_amount", formatAmount(totalAmount));
        bizContent.put("subject", productName);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());

        String form = alipayClient.pageExecute(request).getBody();

        PayOrderEntity payOrderEntity = new PayOrderEntity();
        payOrderEntity.setUserId(userId);
        payOrderEntity.setProductId(productId);
        payOrderEntity.setProductName(productName);
        payOrderEntity.setOutTradeNo(outTradeNo);
        payOrderEntity.setOrderTime(LocalDateTime.now());
        payOrderEntity.setTotalAmount(totalAmount);
        payOrderEntity.setOrderType(OrderTypeEnum.GROUP_BUY);
        payOrderEntity.setOrderStatus(OrderStatusVO.PAY_WAIT);
        payOrderEntity.setPayUrl(form);

        // 幂等落库：首次锁单插入 GROUP_BUY 支付单，复用/重试时仅回写支付表单 + 置为 PAY_WAIT
        if (orderRepository.queryPayOrderByOutTradeNo(outTradeNo) == null) {
            orderRepository.saveGroupBuyPayOrder(payOrderEntity);
        }
        orderRepository.updateOrderPayInfo(payOrderEntity);

        return payOrderEntity;
    }

    /**
     * 修改订单状态-成功
     * @param outTradeNo
     * @param outTradeTime
     */
    @Override
    public void changeOrderPaySuccess(String outTradeNo, Date outTradeTime) {
        orderRepository.changeOrderPaySuccess(outTradeNo, outTradeTime);
    }

    /**
     * 修改订单状态-关单
     * @param outTradeNo
     * @return
     */
    @Override
    public boolean changeOrderPayClose(String outTradeNo) {
        return orderRepository.changeOrderPayClose(outTradeNo);
    }

    /**
     * 超时处理订单：未支付直接关单，已支付调用支付宝退款
     * 不抛异常（供定时任务调用），失败返回 false
     */
    @Transactional
    @Override
    public boolean timeoutCloseOrder(String outTradeNo) {
        PayOrderEntity payOrderEntity = orderRepository.queryPayOrderByOutTradeNo(outTradeNo);
        if (payOrderEntity == null || payOrderEntity.getOutTradeNo() == null) {
            log.warn("超时处理：订单不存在 outTradeNo={}", outTradeNo);
            return false;
        }

        String status = payOrderEntity.getOrderStatus().getCode();

        // 未支付（CREATE / PAY_WAIT）：直接关单
        if (OrderStatusVO.CREATE.getCode().equals(status)
                || OrderStatusVO.PAY_WAIT.getCode().equals(status)) {
            return doTimeoutCloseOrder(outTradeNo);
        }

        // 已支付（PAY_SUCCESS / DEAL_DONE）：调用支付宝退款
        if (OrderStatusVO.PAY_SUCCESS.getCode().equals(status)
                || OrderStatusVO.DEAL_DONE.getCode().equals(status)) {
            return doTimeoutRefundPaidOrder(outTradeNo, payOrderEntity);
        }

        log.info("超时处理：订单状态无需处理 outTradeNo={} status={}", outTradeNo, status);
        return false;
    }

    /**
     * 超时关单（未支付）
     */
    private boolean doTimeoutCloseOrder(String outTradeNo) {
        boolean closed = orderRepository.changeOrderPayClose(outTradeNo);
        if (closed) {
            log.info("超时关单成功 outTradeNo={}", outTradeNo);
        } else {
            log.warn("超时关单失败（状态已变化） outTradeNo={}", outTradeNo);
        }
        return closed;
    }

    /**
     * 超时退款（已支付）：乐观锁占位 → 支付宝退款 → 更新状态
     */
    private boolean doTimeoutRefundPaidOrder(String outTradeNo, PayOrderEntity payOrderEntity) {
        // 乐观锁：PAY_SUCCESS/DEAL_DONE → REFUNDING
        if (!orderRepository.changeOrderRefunding(outTradeNo)) {
            log.info("超时退款：已有退款在处理中，跳过 outTradeNo={}", outTradeNo);
            return false;
        }
        String fromStatus = payOrderEntity.getOrderStatus().getCode();
        try {
            boolean refundSuccess = alipayRefundPort.refund(outTradeNo, null, payOrderEntity.getTotalAmount());
            if (!refundSuccess) {
                orderRepository.changeOrderRefundResult(outTradeNo, OrderStatusVO.REFUNDING.getCode(), fromStatus);
                log.error("超时退款：支付宝退款失败 outTradeNo={}", outTradeNo);
                return false;
            }
            orderRepository.changeOrderRefundResult(outTradeNo, OrderStatusVO.REFUNDING.getCode(), OrderStatusVO.REFUND.getCode());
            log.info("超时退款成功 outTradeNo={}", outTradeNo);
            return true;
        } catch (Exception e) {
            orderRepository.changeOrderRefundResult(outTradeNo, OrderStatusVO.REFUNDING.getCode(), fromStatus);
            log.error("超时退款异常 outTradeNo={}", outTradeNo, e);
            return false;
        }
    }

    /**
     * 查询未支付订单-用作回调任务
     * @return
     */
    @Override
    public List<String> queryNoPayNotifyOrderList() {
        return orderRepository.queryNoPayNotifyOrderList();
    }

    /**
     * 查询超时订单-关单
     * @return
     */
    @Override
    public List<String> queryTimeOutCloseOrderList() {
        return orderRepository.queryTimeOutCloseOrderList();
    }

    /**
     * 退单-已支付
     * @param userId 当前登录用户（用于订单归属校验）
     * @param outTradeNo
     * @return
     */
    @Override
    public boolean refundOrder(String userId, String outTradeNo) {
        PayOrderEntity payOrderEntity = orderRepository.queryPayOrderByOutTradeNo(outTradeNo);
        if (payOrderEntity == null || payOrderEntity.getOutTradeNo() == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "退单订单不存在");
        }
        // 订单归属校验：防止越权退他人的单
        if (payOrderEntity.getUserId() == null || !payOrderEntity.getUserId().equals(userId)) {
            throw new AppException(ResponseCode.FORBIDDEN, "无权退该订单");
        }
        // 按类型选择方法
        if (OrderStatusVO.PAY_SUCCESS.equals(payOrderEntity.getOrderStatus())
                || OrderStatusVO.DEAL_DONE.equals(payOrderEntity.getOrderStatus())) {
            // ---- 已支付退单：需要真正调支付宝退款 ----
            return doRefundPaidOrder(outTradeNo, payOrderEntity);
        } else if (OrderStatusVO.PAY_WAIT.equals(payOrderEntity.getOrderStatus())
                || OrderStatusVO.CREATE.equals(payOrderEntity.getOrderStatus())) {
            // ---- 未支付退单：直接关单，不用退钱 ----
            return doCloseUnpaidOrder(outTradeNo);
        }
        throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "当前订单状态不可退单");
    }

    /**
     * 支付宝 total_amount 仅允许最多两位小数；折扣试算的 BigDecimal 乘法可能产生 3 位以上小数（如 90.000），
     * 必须归一化，否则网关校验失败跳 /error
     */
    private static String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    // 已支付退单
    private boolean doRefundPaidOrder(String outTradeNo, PayOrderEntity payOrderEntity) {
        // 数据库乐观锁占位：只允许一个请求把 PAY_SUCCESS/DEAL_DONE -> REFUNDING
        // 影响 0 行说明已有人在退或已退，直接判为重复请求
        if(!orderRepository.changeOrderRefunding(outTradeNo)) {
            throw new AppException(ResponseCode.CONFLICT, "该订单退款已受理，请勿重复操作");
        }
        // 记录原支付状态，退款失败时回滚
        String fromStatus = payOrderEntity.getOrderStatus().getCode();
        try {
            boolean isRefundSuccess = alipayRefundPort.refund(outTradeNo, null, payOrderEntity.getTotalAmount());
            if (!isRefundSuccess) {
                // 失败：回滚 REFUNDING -> 原状态，允许用户重试
                orderRepository.changeOrderRefundResult(outTradeNo, OrderStatusVO.REFUNDING.getCode(), fromStatus);
                throw new AppException(ResponseCode.UN_ERROR, "支付宝退款失败");
            }
            // 成功：REFUNDING -> REFUND
            orderRepository.changeOrderRefundResult(outTradeNo, OrderStatusVO.REFUNDING.getCode(), OrderStatusVO.REFUND.getCode());
            return true;
        } finally {
            alipayPort.unlock(outTradeNo);
        }
    }
    // 未支付退单
    private boolean doCloseUnpaidOrder(String outTradeNo) {
        return orderRepository.changeOrderPayClose(outTradeNo);
    }

    @Override
    public List<PayOrderEntity> queryPageByStatusAndUserId(String status, String userId, Integer pageNo, Integer pageSize) {
        // 参数校验：pageNo 最小 1，pageSize 默认 10 上限 100
        int safePageNo = (pageNo == null || pageNo <= 0) ? 1 : pageNo;
        int safePageSize = (pageSize == null || pageSize <= 0) ? 10 : Math.min(pageSize, 100);
        int offset = (safePageNo - 1) * safePageSize;
        return orderRepository.queryPageByStatusAndUserId(status, userId, offset, safePageSize);
    }

    @Override
    public long countByStatusAndUserId(String status, String userId) {
        return orderRepository.countByStatusAndUserId(status, userId);
    }

    @Override
    public PayOrderEntity queryByUserIdAndOutTradeNo(String userId, String outTradeNo) {
        PayOrderEntity entity = orderRepository.queryPayOrderByOutTradeNo(outTradeNo);
        if (entity == null || !userId.equals(entity.getUserId())) {
            return null;
        }
        return entity;
    }

    @Override
    public boolean closeOrder(String userId, String outTradeNo) {
        PayOrderEntity entity = queryByUserIdAndOutTradeNo(userId, outTradeNo);
        if (entity == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "订单不存在");
        }
        // 仅 CREATE / PAY_WAIT 可关单
        if (!OrderStatusVO.CREATE.equals(entity.getOrderStatus())
                && !OrderStatusVO.PAY_WAIT.equals(entity.getOrderStatus())) {
            throw new AppException(ResponseCode.UN_ERROR, "当前订单状态不可关闭");
        }
        return orderRepository.changeOrderPayClose(outTradeNo);
    }

    @Override
    public PayOrderEntity repayOrder(String userId, String outTradeNo) throws AlipayApiException {
        PayOrderEntity entity = queryByUserIdAndOutTradeNo(userId, outTradeNo);
        if (entity == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "订单不存在");
        }
        // 仅 CREATE / PAY_WAIT 可再次支付
        if (!OrderStatusVO.CREATE.equals(entity.getOrderStatus())
                && !OrderStatusVO.PAY_WAIT.equals(entity.getOrderStatus())) {
            throw new AppException(ResponseCode.UN_ERROR, "订单状态已变化，请刷新后重试");
        }

        // 幂等重生成支付宝支付表单，复用 doPrepayOrder 的核心逻辑
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(returnUrl);
        request.setNotifyUrl(notifyUrl);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("total_amount", formatAmount(entity.getTotalAmount()));
        bizContent.put("subject", entity.getProductName());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());

        String form = alipayClient.pageExecute(request).getBody();

        PayOrderEntity payOrderEntity = PayOrderEntity.builder()
                .userId(userId)
                .productId(entity.getProductId())
                .productName(entity.getProductName())
                .outTradeNo(outTradeNo)
                .orderTime(entity.getOrderTime())
                .totalAmount(entity.getTotalAmount())
                .orderType(entity.getOrderType())
                .orderStatus(OrderStatusVO.PAY_WAIT)
                .payUrl(form)
                .build();

        orderRepository.updateOrderPayInfo(payOrderEntity);

        return payOrderEntity;
    }
}



