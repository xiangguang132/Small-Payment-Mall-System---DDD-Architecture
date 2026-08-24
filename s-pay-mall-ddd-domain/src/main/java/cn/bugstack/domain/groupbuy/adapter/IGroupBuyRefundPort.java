package cn.bugstack.domain.groupbuy.adapter;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderBehaviorEntity;

/**
 * 拼团退单回调端口
 */
public interface IGroupBuyRefundPort {

    /**
     * 退单回调
     * @param behaviorEntity 退单行为结果
     * @throws Exception 业务回调异常
     */
    void groupBuyRefundNotify(GroupBuyRefundOrderBehaviorEntity behaviorEntity) throws Exception;
}
