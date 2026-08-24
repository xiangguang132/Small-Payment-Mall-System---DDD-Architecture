package cn.bugstack.domain.groupbuy.service.refund.business;

import cn.bugstack.domain.groupbuy.adapter.IGroupBuyRefundPort;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderBehaviorEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderEntity;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

@Slf4j
public abstract class AbstractGroupBuyRefundOrderStrategy implements IGroupBuyRefundOrderStrategy {

    @Resource
    protected IGroupBuyRefundPort groupBuyRefundPort;

    /**
     * 统一退单后回调钩子。
     * 子类完成各自退款/关单逻辑后，构造行为结果并调用这里即可。
     */
    protected void sendRefundNotifyMessage(GroupBuyRefundOrderEntity refundOrderEntity, boolean success, String message) {
        GroupBuyRefundOrderBehaviorEntity behaviorEntity = GroupBuyRefundOrderBehaviorEntity.builder()
                .userId(refundOrderEntity.getUserId())
                .teamId(refundOrderEntity.getTeamId())
                .activityId(refundOrderEntity.getActivityId())
                .orderId(refundOrderEntity.getOrderId())
                .success(success)
                .message(message)
                .build();
        sendRefundNotifyMessage(behaviorEntity);
    }

    /**
     * 统一退单后回调钩子。
     */
    protected void sendRefundNotifyMessage(GroupBuyRefundOrderBehaviorEntity behaviorEntity) {
        if (behaviorEntity == null) {
            return;
        }
        try {
            groupBuyRefundPort.groupBuyRefundNotify(behaviorEntity);
        } catch (Exception e) {
            log.error("拼团退单回调失败 userId:{} teamId:{} orderId:{} message:{}",
                    behaviorEntity.getUserId(),
                    behaviorEntity.getTeamId(),
                    behaviorEntity.getOrderId(),
                    behaviorEntity.getMessage(),
                    e);
        }
    }
}
