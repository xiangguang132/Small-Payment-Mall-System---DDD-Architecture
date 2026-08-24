package cn.bugstack.domain.groupbuy.service.refund.business.impl;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderEntity;
import cn.bugstack.domain.groupbuy.service.refund.business.AbstractGroupBuyRefundOrderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("paidTeamRefundStrategy")
public class PaidTeamRefundStrategy extends AbstractGroupBuyRefundOrderStrategy {

    @Override
    public void refundGroupBuyOrder(GroupBuyRefundOrderEntity groupBuyRefundOrderEntity) {
        log.info("退单：已支付成已团 userId:{} teamId:{} orderId:{} ", groupBuyRefundOrderEntity.getUserId(), groupBuyRefundOrderEntity.getTeamId(), groupBuyRefundOrderEntity.getOutTradeNo());

        // todo已支付成已团

        // todo mq消息
    }

    // todo 恢复库存
}
