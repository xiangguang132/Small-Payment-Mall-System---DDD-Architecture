package cn.bugstack.domain.groupbuy.service.refund.business;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderEntity;

/**
 * 退单策略接口
 * 未支付：Unpaid
 * 未成团：UnformedTeam
 * 已成团：AlreadyFormTeam
 */
public interface IGroupBuyRefundOrderStrategy {

    void refundGroupBuyOrder(GroupBuyRefundOrderEntity groupBuyRefundOrderEntity);

    /**
     * 恢复退单占用的锁单量（由 MQ 消费端调用）
     * @param restoreEntity
     * @throws Exception
     */
    void reverseStock(GroupBuyRefundRestoreEntity restoreEntity) throws Exception;

}
