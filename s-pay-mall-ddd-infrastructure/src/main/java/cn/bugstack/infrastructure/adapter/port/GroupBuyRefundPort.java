package cn.bugstack.infrastructure.adapter.port;

import cn.bugstack.domain.groupbuy.adapter.IGroupBuyRefundPort;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderBehaviorEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GroupBuyRefundPort implements IGroupBuyRefundPort {

    @Override
    public void groupBuyRefundNotify(GroupBuyRefundOrderBehaviorEntity behaviorEntity) {
        log.info("拼团退单回调 userId:{} teamId:{} orderId:{} activityId:{} success:{} message:{}",
                behaviorEntity.getUserId(),
                behaviorEntity.getTeamId(),
                behaviorEntity.getOrderId(),
                behaviorEntity.getActivityId(),
                behaviorEntity.isSuccess(),
                behaviorEntity.getMessage());
    }
}
