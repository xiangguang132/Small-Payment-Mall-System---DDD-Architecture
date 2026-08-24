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
        log.info("退单：未支付未成团 userId:{} teamId:{} outTradeNo:{}",
                groupBuyRefundOrderEntity.getUserId(),
                groupBuyRefundOrderEntity.getTeamId(),
                groupBuyRefundOrderEntity.getOutTradeNo());

        // 未支付：不传 payAmount（null），Port 层跳过支付宝退款，直接恢复库存并更新状态
        sendRefundNotifyMessage(groupBuyRefundOrderEntity, true, "未支付退单成功");
    }

}
