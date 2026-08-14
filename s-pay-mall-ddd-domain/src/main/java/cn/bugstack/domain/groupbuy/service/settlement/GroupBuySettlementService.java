package cn.bugstack.domain.groupbuy.service.settlement;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuySettlementCommandEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuySettlementFeedBackEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTeamEntity;
import cn.bugstack.domain.groupbuy.model.aggregate.GroupBuyTeamSettlementAggregate;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyOrderRepository;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyTeamRepository;
import cn.bugstack.domain.groupbuy.model.valobj.GroupBuyProgressVO;
import cn.bugstack.domain.groupbuy.service.settlement.factory.GroupBuySettlementRuleFilterFactory;
import cn.bugstack.types.design.framework.link.multilink.chain.BusinessLinkedList;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 结算服务实现
 */
@Service
public class GroupBuySettlementService implements IGroupBuySettlementService {

    @Resource(name = "groupBuySettlementRuleFilter")
    private BusinessLinkedList<GroupBuySettlementCommandEntity, GroupBuySettlementRuleFilterFactory.DynamicContext, GroupBuySettlementFeedBackEntity> groupBuySettlementRuleFilter;

    @Resource
    private IGroupBuyOrderRepository groupBuyOrderRepository;

    @Resource
    private IGroupBuyTeamRepository groupBuyTeamRepository;

    /**
     * 结算订单
     * @param command
     * @return
     * @throws Exception
     */
    @Override
    public GroupBuySettlementFeedBackEntity settlementGroupBuyOrder(GroupBuySettlementCommandEntity command) throws Exception {
        // 获取 结算过滤链 的结果
        GroupBuySettlementFeedBackEntity feedBackEntity = groupBuySettlementRuleFilter.apply(
                command,
                new GroupBuySettlementRuleFilterFactory.DynamicContext()
        );
        if (feedBackEntity == null) {
            throw new AppException(ResponseCode.UN_ERROR, "拼团组队结算规则链未返回结果");
        }

        // 结果没问题的话，构建团队快照并交给仓储完成结算
        GroupBuyTeamEntity team = GroupBuyTeamEntity.builder()
                .teamId(feedBackEntity.getTeamId())
                .activityId(feedBackEntity.getActivityId())
                .targetCount(feedBackEntity.getTargetCount())
                .completeCount(feedBackEntity.getCompleteCount())
                .lockCount(feedBackEntity.getLockCount())
                .status(feedBackEntity.getStatus())
                .validStartTime(feedBackEntity.getValidStartTime())
                .validEndTime(feedBackEntity.getValidEndTime())
                .notifyUrl(feedBackEntity.getNotifyUrl())
                .build();

        GroupBuyTeamSettlementAggregate aggregate = GroupBuyTeamSettlementAggregate.builder()
                .settlementCommand(command)
                .groupBuyTeamEntity(team)
                .build();

        boolean complete = groupBuyOrderRepository.settlementGroupBuyOrder(aggregate);
        feedBackEntity.setComplete(complete);
        return feedBackEntity;
    }

    /**
     * 查询拼团过程
     * @param teamId
     * @return
     */
    @Override
    public GroupBuyProgressVO queryGroupBuyProgress(String teamId) {
        GroupBuyTeamEntity team = groupBuyTeamRepository.queryGroupBuyTeamByTeamId(teamId);
        if (team == null) {
            return null;
        }
        return GroupBuyProgressVO.builder()
                .teamId(team.getTeamId())
                .targetCount(team.getTargetCount())
                .completeCount(team.getCompleteCount())
                .lockCount(team.getLockCount())
                .status(team.getStatus())
                .validEndTime(team.getValidEndTime())
                .build();
    }
}
