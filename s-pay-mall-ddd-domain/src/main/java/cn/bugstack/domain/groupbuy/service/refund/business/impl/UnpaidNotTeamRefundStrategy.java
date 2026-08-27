package cn.bugstack.domain.groupbuy.service.refund.business.impl;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundRestoreEntity;
import cn.bugstack.domain.groupbuy.service.refund.business.AbstractGroupBuyRefundOrderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("unpaidNotTeamRefundStrategy")
public class UnpaidNotTeamRefundStrategy extends AbstractGroupBuyRefundOrderStrategy {

    @Override
    public void refundGroupBuyOrder(GroupBuyRefundOrderEntity e) {
        log.info("退单：未支付未成团，无需调用支付宝退款（无实付款） userId:{} teamId:{} outTradeNo:{}", e.getUserId(), e.getTeamId(), e.getOutTradeNo());
        sendRefundNotifyMessage(e, "unpaidNotTeamRefundStrategy", true, "未支付退单成功");   // payAmount 不传
    }
    @Override
    public void reverseStock(GroupBuyRefundRestoreEntity restoreEntity) {
        doReverseStock(restoreEntity);   // 锁单过，要恢复
    }

}
