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

}
