package cn.bugstack.domain.groupbuy.service.refund.business.impl;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderEntity;
import cn.bugstack.domain.groupbuy.service.refund.business.AbstractGroupBuyRefundOrderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("paidRefundStrategy")
public class PaidRefundStrategy extends AbstractGroupBuyRefundOrderStrategy {

    @Override
    public void refundGroupBuyOrder(GroupBuyRefundOrderEntity groupBuyRefundOrderEntity) {
        log.info("退单：已支付未成团 userId:{} teamId:{} orderId:{} ", groupBuyRefundOrderEntity.getUserId(), groupBuyRefundOrderEntity.getTeamId(), groupBuyRefundOrderEntity.getOutTradeNo());
        // todo 退单与发送mq
    }

    // todo 恢复库存
}
