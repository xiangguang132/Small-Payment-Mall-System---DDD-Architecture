package cn.bugstack.domain.groupbuy.service.refund;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderBehaviorEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderCommandEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundRestoreEntity;

public interface IGroupBuyRefundOrderService {

    /**
     * 拼团退单
     * @param groupBuyRefundOrderCommandEntity
     * @return
     */
    GroupBuyRefundOrderBehaviorEntity refundGroupBuyOrder(GroupBuyRefundOrderCommandEntity groupBuyRefundOrderCommandEntity);

    /**
     * 恢复锁单量（MQ 消费端调用）
     */
    void restoreTeamLockStock(GroupBuyRefundRestoreEntity restoreEntity) throws Exception;
}
