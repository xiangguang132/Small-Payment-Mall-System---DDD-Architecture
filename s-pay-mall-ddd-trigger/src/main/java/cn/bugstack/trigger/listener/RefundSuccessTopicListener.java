package cn.bugstack.trigger.listener;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundRestoreEntity;
import cn.bugstack.domain.groupbuy.service.refund.IGroupBuyRefundOrderService;
import com.alibaba.fastjson.JSON;
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
public class RefundSuccessTopicListener {

    @Resource
    private IGroupBuyRefundOrderService groupBuyRefundOrderService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${spring.rabbitmq.config.producer.topic_team_refund.queue}"),
                    exchange = @Exchange(value = "${spring.rabbitmq.config.producer.exchange}", type = ExchangeTypes.TOPIC),
                    key = "${spring.rabbitmq.config.producer.topic_team_refund.routing_key}"
            )
    )
    public void listener(String message) {
        log.info("接收消息（退单成功）- 恢复拼团队伍锁单量:{}", message);
        GroupBuyRefundRestoreEntity restoreEntity = JSON.parseObject(message, GroupBuyRefundRestoreEntity.class);
        try {
            groupBuyRefundOrderService.restoreTeamLockStock(restoreEntity);
        } catch (Exception e) {
            log.error("接收消息（退单成功）- 恢复拼团队伍锁单量失败: message:{}", message, e);
            throw new RuntimeException(e);   // 抛异常，MQ 重试
        }
    }
}

