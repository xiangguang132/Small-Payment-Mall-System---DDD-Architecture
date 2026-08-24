package cn.bugstack.domain.groupbuy.service.refund.business.impl;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderEntity;
import cn.bugstack.domain.groupbuy.service.refund.business.AbstractGroupBuyRefundOrderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("unpaidNotTeamRefundStrategy")
public class UnpaidNotTeamRefundStrategy extends AbstractGroupBuyRefundOrderStrategy {

    @Override
    public void refundGroupBuyOrder(GroupBuyRefundOrderEntity groupBuyRefundOrderEntity) {
        log.info("退单：未支付成已团 userId:{} teamId:{} orderId:{} ", groupBuyRefundOrderEntity.getUserId(), groupBuyRefundOrderEntity.getTeamId(), groupBuyRefundOrderEntity.getOutTradeNo());
        // todo 未支付成已团

        sendRefundNotifyMessage(groupBuyRefundOrderEntity, true, "未支付退单成功");
    }

    // todo 恢复库存
}
