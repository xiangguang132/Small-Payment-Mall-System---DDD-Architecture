package cn.bugstack.domain.groupbuy.service.refund.business.impl;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderEntity;
import cn.bugstack.domain.groupbuy.service.refund.business.AbstractGroupBuyRefundOrderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("paidTeamRefundStrategy")
public class PaidTeamRefundStrategy extends AbstractGroupBuyRefundOrderStrategy {

    @Override
    public void refundGroupBuyOrder(GroupBuyRefundOrderEntity e) {
        log.info("退单：已支付已成团 userId:{} teamId:{} outTradeNo:{}", e.getUserId(), e.getTeamId(), e.getOutTradeNo());
        sendRefundNotifyMessage(e, "paidTeamRefundStrategy", true, "已支付已成团退单成功", e.getPayAmount());
    }
    @Override
    public void reverseStock(GroupBuyRefundRestoreEntity restoreEntity) {
        log.info("退单；已支付已成团，队伍组队结束，不需要恢复锁单量 teamId:{}", restoreEntity.getTeamId());
    }

}
