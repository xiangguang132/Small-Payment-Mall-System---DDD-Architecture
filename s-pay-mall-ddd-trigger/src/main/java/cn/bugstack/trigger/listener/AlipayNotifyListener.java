package cn.bugstack.trigger.listener;

import cn.bugstack.domain.payment.service.IAlipayNotifyTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class AlipayNotifyListener {

    @Resource
    private IAlipayNotifyTaskService alipayNotifyTaskService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${spring.rabbitmq.config.producer.topic_alipay_notify.queue}"),
                    exchange = @Exchange(value = "${spring.rabbitmq.config.producer.exchange}", type = ExchangeTypes.TOPIC),
                    key = "${spring.rabbitmq.config.producer.topic_alipay_notify.routing_key}"
            )
    )
    public void listener(String message) {
        log.info("接收消息（支付宝异步通知）:{}", message);
        try {
            alipayNotifyTaskService.processTask(message);
        } catch (Exception e) {
            log.error("处理支付宝异步通知失败 message:{}", message, e);
            throw e;
        }
    }

}
