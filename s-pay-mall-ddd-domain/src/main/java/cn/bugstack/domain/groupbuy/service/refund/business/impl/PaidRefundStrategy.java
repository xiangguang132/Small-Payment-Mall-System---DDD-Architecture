package cn.bugstack.domain.groupbuy.service.refund.business.impl;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundRestoreEntity;
import cn.bugstack.domain.groupbuy.service.refund.business.AbstractGroupBuyRefundOrderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("paidRefundStrategy")
public class PaidRefundStrategy extends AbstractGroupBuyRefundOrderStrategy {

    @Override
    public void refundGroupBuyOrder(GroupBuyRefundOrderEntity e) {
        log.info("退单：已支付未成团，即将调用支付宝退款 payAmount:{} userId:{} teamId:{} outTradeNo:{}", e.getPayAmount(), e.getUserId(), e.getTeamId(), e.getOutTradeNo());
        sendRefundNotifyMessage(e, "paidRefundStrategy", true, "已支付未成团退单成功", e.getPayAmount());
    }
    @Override
    public void reverseStock(GroupBuyRefundRestoreEntity restoreEntity) {
        doReverseStock(restoreEntity);   // 未成团，要恢复锁单量
    }
}
