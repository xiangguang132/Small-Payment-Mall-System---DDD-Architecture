package cn.bugstack.domain.groupbuy.service.settlement.filter;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuySettlementCommandEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuySettlementFeedBackEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTeamEntity;
import cn.bugstack.domain.groupbuy.service.settlement.factory.GroupBuySettlementRuleFilterFactory;
import cn.bugstack.types.design.framework.link.multilink.handler.ILogicHandler;
import org.springframework.stereotype.Service;

@Service
public class EndRuleFilter implements ILogicHandler<
        GroupBuySettlementCommandEntity,
        GroupBuySettlementRuleFilterFactory.DynamicContext,
        GroupBuySettlementFeedBackEntity> {

    @Override
    public GroupBuySettlementFeedBackEntity apply(
            GroupBuySettlementCommandEntity requestParameter,
            GroupBuySettlementRuleFilterFactory.DynamicContext dynamicContext) {
        GroupBuyTeamEntity team = dynamicContext.getGroupBuyTeamEntity();
        return GroupBuySettlementFeedBackEntity.builder()
                .teamId(team.getTeamId())
                .activityId(team.getActivityId())
                .targetCount(team.getTargetCount())
                .completeCount(team.getCompleteCount())
                .lockCount(team.getLockCount())
                .status(team.getStatus())
                .validStartTime(team.getValidStartTime())
                .validEndTime(team.getValidEndTime())
                .notifyUrl(team.getNotifyUrl())
                .build();
    }
}
