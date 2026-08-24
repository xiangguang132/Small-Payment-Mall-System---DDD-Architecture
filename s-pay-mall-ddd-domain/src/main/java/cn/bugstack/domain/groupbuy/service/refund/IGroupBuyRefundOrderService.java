package cn.bugstack.domain.groupbuy.service.refund;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderBehaviorEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderCommandEntity;

public interface IGroupBuyRefundOrderService {

    /**
     * 拼团退单
     * @param groupBuyRefundOrderCommandEntity
     * @return
     */
    GroupBuyRefundOrderBehaviorEntity refundGroupBuyOrder(GroupBuyRefundOrderCommandEntity groupBuyRefundOrderCommandEntity);

//    void restoreGroupBuyLockStock()

}
