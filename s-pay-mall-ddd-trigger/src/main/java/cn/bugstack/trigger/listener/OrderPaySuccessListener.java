package cn.bugstack.trigger.listener;

import cn.bugstack.infrastructure.dao.ICartDao;
import cn.bugstack.infrastructure.dao.IUserCouponDao;
import cn.bugstack.infrastructure.dao.IOrderDao;
import cn.bugstack.infrastructure.dao.IPayOrderItemDao;
import cn.bugstack.infrastructure.dao.po.payment.PayOrder;
import cn.bugstack.infrastructure.dao.po.payment.PayOrderItem;
import cn.bugstack.types.enums.OrderTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 支付成功回调消息 —— 统一核销优惠券（直购 + 拼团）
 */
@Slf4j
@Component
public class OrderPaySuccessListener {

    @Resource
    private IOrderDao orderDao;
    @Resource
    private IUserCouponDao userCouponDao;
    @Resource
    private IPayOrderItemDao payOrderItemDao;
    @Resource
    private ICartDao cartDao;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${spring.rabbitmq.config.producer.topic_order_pay_success.queue}"),
                    exchange = @Exchange(value = "${spring.rabbitmq.config.producer.exchange}", type = ExchangeTypes.TOPIC),
                    key = "${spring.rabbitmq.config.producer.topic_order_pay_success.routing_key}"
            )
    )
    public void handleEvent(String message) {
        String outTradeNo = message;
        log.info("收到支付成功消息 outTradeNo:{}", outTradeNo);

        PayOrder payOrder = orderDao.queryPayOrderByOutTradeNo(outTradeNo);
        if (payOrder == null) {
            return;
        }

        // 1. 核销优惠券：从 pay_order 读取 couponIds，标记为已使用（直购 + 拼团 + 购物车统一）
        try {
            String couponIdsJson = payOrder.getCouponIds();
            if (couponIdsJson == null || couponIdsJson.isEmpty() || "[]".equals(couponIdsJson)) {
                return;
            }
            List<String> couponIds = com.alibaba.fastjson.JSON.parseArray(couponIdsJson, String.class);
            if (couponIds == null || couponIds.isEmpty()) {
                return;
            }

            int updated = userCouponDao.batchUpdateUserCouponUsed(
                    payOrder.getUserId(),
                    couponIds,
                    outTradeNo,
                    LocalDateTime.now()
            );
            log.info("支付成功-优惠券核销 userId:{} couponIds:{} 核销数量:{} orderType:{}",
                    payOrder.getUserId(), couponIds, updated, payOrder.getOrderType());
        } catch (Exception e) {
            log.error("支付成功-优惠券核销异常 outTradeNo:{}", outTradeNo, e);
        }

        // 2. 清理已结算购物车（仅 CART 类型，按明细反查 productIds）
        if (OrderTypeEnum.CART.getCode().equals(payOrder.getOrderType())) {
            try {
                List<PayOrderItem> items = payOrderItemDao.queryByOrderId(outTradeNo);
                if (items != null && !items.isEmpty()) {
                    List<Long> productIds = items.stream()
                            .map(PayOrderItem::getProductId)
                            .collect(Collectors.toList());
                    cartDao.deleteByUserIdAndProductIds(payOrder.getUserId(), productIds);
                    log.info("支付成功-清理购物车 userId:{} productIds:{}", payOrder.getUserId(), productIds);
                }
            } catch (Exception e) {
                log.error("支付成功-清理购物车异常 outTradeNo:{}", outTradeNo, e);
            }
        }
    }

}
