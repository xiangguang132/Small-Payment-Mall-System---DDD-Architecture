package cn.bugstack.domain.groupbuy.service.refund.business;

import cn.bugstack.domain.groupbuy.adapter.IGroupBuyRefundPort;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderBehaviorEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderEntity;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
public abstract class AbstractGroupBuyRefundOrderStrategy implements IGroupBuyRefundOrderStrategy {

    @Resource
    protected IGroupBuyRefundPort groupBuyRefundPort;

    /**
     * 统一退单后回调钩子（无需退款金额的场景，如未支付退单）。
     */
    protected void sendRefundNotifyMessage(GroupBuyRefundOrderEntity refundOrderEntity, boolean success, String message) {
        sendRefundNotifyMessage(refundOrderEntity, success, message, null);
    }

    /**
     * 统一退单后回调钩子（需要退款金额的场景，如已支付退单）。
     */
    protected void sendRefundNotifyMessage(GroupBuyRefundOrderEntity refundOrderEntity, boolean success, String message, BigDecimal payAmount) {
        GroupBuyRefundOrderBehaviorEntity behaviorEntity = GroupBuyRefundOrderBehaviorEntity.builder()
                .userId(refundOrderEntity.getUserId())
                .teamId(refundOrderEntity.getTeamId())
                .activityId(refundOrderEntity.getActivityId())
                .orderId(refundOrderEntity.getOrderId())
                .outTradeNo(refundOrderEntity.getOutTradeNo())
                .payAmount(payAmount)
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

    /**
     * 统一退单后回调钩子（带退单类型，供 MQ 消息记录 type）。
     */
    protected void sendRefundNotifyMessage(GroupBuyRefundOrderEntity groupBuyRefundOrderEntity, String refundType, boolean success, String message, BigDecimal payAmount) {
        GroupBuyRefundOrderBehaviorEntity groupBuyRefundOrderBehaviorEntity = GroupBuyRefundOrderBehaviorEntity.builder()
                .userId(groupBuyRefundOrderEntity.getUserId())
                .teamId(groupBuyRefundOrderEntity.getTeamId())
                .activityId(groupBuyRefundOrderEntity.getActivityId())
                .orderId(groupBuyRefundOrderEntity.getOrderId())
                .outTradeNo(groupBuyRefundOrderEntity.getOutTradeNo())
                .payAmount(payAmount)
                .refundType(refundType)
                .success(success)
                .message(message)
                .build();
    }

    /**
     * 消费端恢复锁单量（对标参考 doReverseStock；本地无 Redis，直接扣 DB team.lock_count）。
     */
    protected void doReverseStock(GroupBuyRefundRestoreEntity restoreEntity) {
        log.info("退单；恢复锁单量 activityId:{} teamId:{}",
                restoreEntity.getActivityId(), restoreEntity.getTeamId());
        int updated = groupBuyTeamRepository.updateSubtractLockCount(restoreEntity.getTeamId());
        if (updated != 1) {
            log.warn("退单；恢复锁单量失败或已恢复 teamId:{}", restoreEntity.getTeamId());
        }
    }
}
