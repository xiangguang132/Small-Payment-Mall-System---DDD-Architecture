package cn.bugstack.trigger.job;

import cn.bugstack.domain.payment.adapter.repository.IAlipayNotifyTaskRepository;
import cn.bugstack.infrastructure.event.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class AlipayNotifyJob {

    @Resource
    private IAlipayNotifyTaskRepository repository;
    @Resource
    private EventPublisher eventPublisher;
    @Value("${spring.rabbitmq.config.producer.topic_alipay_notify.routing_key}")
    private String routingKey;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void exec() {
        try {
            List<String> outTradeNos = repository.queryRetryOutTradeNoList();
            if (outTradeNos == null || outTradeNos.isEmpty()) {
                return;
            }
            for (String outTradeNo : outTradeNos) {
                eventPublisher.publish(routingKey, outTradeNo);
                log.info("重新投递支付宝通知任务 outTradeNo:{}", outTradeNo);
            }
        } catch (Exception e) {
            log.error("重新投递支付宝通知任务失败", e);
        }
    }

}
