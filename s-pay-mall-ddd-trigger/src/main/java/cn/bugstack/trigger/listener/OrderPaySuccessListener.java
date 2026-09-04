package cn.bugstack.trigger.listener;

import cn.bugstack.infrastructure.dao.IUserCouponDao;
import cn.bugstack.infrastructure.dao.IOrderDao;
import cn.bugstack.infrastructure.dao.po.payment.PayOrder;
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

        // 核销优惠券：从 pay_order 读取 couponIds，标记为已使用
        try {
            PayOrder payOrder = orderDao.queryPayOrderByOutTradeNo(outTradeNo);
            if (payOrder == null) {
                return;
            }
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
    }

}
