package cn.bugstack.trigger.listener;

import cn.bugstack.domain.groupbuy.service.user.IUserNotifyTaskService;
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
public class GroupBuySuccessTopicListener {

    @Resource
    private IUserNotifyTaskService userNotifyTaskService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${spring.rabbitmq.config.producer.topic_team_success.queue}"),
                    exchange = @Exchange(value = "${spring.rabbitmq.config.producer.exchange}", type = ExchangeTypes.TOPIC),
                    key = "${spring.rabbitmq.config.producer.topic_team_success.routing_key}"
            )
    )
    public void listener(String message) {
        log.info("接收消息（站内信异步通知）:{}", message);
        try {
            userNotifyTaskService.writeTeamSuccessNotify(message);
        } catch (Exception e) {
            // 处理失败的消息直接确认（ack），不 requeue 死循环；
            // 重试节奏交由 GroupBuyNotifyJob 定时扫描 task_status=0 的任务补偿。
            log.error("成团站内信写入失败，交由定时任务补偿 message:{}", message, e);
        }
    }

}
