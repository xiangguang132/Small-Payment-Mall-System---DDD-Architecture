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
        log.info("退单：已支付已成团 userId:{} teamId:{} outTradeNo:{}",
                groupBuyRefundOrderEntity.getUserId(),
                groupBuyRefundOrderEntity.getTeamId(),
                groupBuyRefundOrderEntity.getOutTradeNo());

        // 传入 payAmount，由 Port 层调用支付宝退款并处理库存恢复与状态更新
        sendRefundNotifyMessage(groupBuyRefundOrderEntity, true,
                "已支付已成团退单成功", groupBuyRefundOrderEntity.getPayAmount());
    }

}
