package cn.bugstack.domain.groupbuy.service.settlement.filter;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyOrderEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuySettlementCommandEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuySettlementFeedBackEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTeamEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyTeamRepository;
import cn.bugstack.domain.groupbuy.service.settlement.factory.GroupBuySettlementRuleFilterFactory;
import cn.bugstack.types.design.framework.link.multilink.handler.ILogicHandler;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 过滤节点-判断时间
 */
@Service
public class SettableRuleFilter implements ILogicHandler<GroupBuySettlementCommandEntity, GroupBuySettlementRuleFilterFactory.DynamicContext, GroupBuySettlementFeedBackEntity> {

    @Resource
    private IGroupBuyTeamRepository groupBuyTeamRepository;

    @Override
    public GroupBuySettlementFeedBackEntity apply(GroupBuySettlementCommandEntity requestParameter, GroupBuySettlementRuleFilterFactory.DynamicContext dynamicContext) throws Exception {

        // 获取 订单 与 组团 信息并判空
        GroupBuyOrderEntity orderEntity = dynamicContext.getGroupBuyOrderEntity();
        GroupBuyTeamEntity teamEntity = orderEntity == null
                ? null
                : groupBuyTeamRepository.queryGroupBuyTeamByTeamId(orderEntity.getTeamId());
        if (!validOrderAndTeam(orderEntity, teamEntity)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "请求订单或组团信息为空");
        }

        LocalDateTime payTime = requestParameter.getPayTime();
        if (payTime != null
                && teamEntity.getValidEndTime() != null
                && !payTime.isBefore(teamEntity.getValidEndTime())) {
            throw new AppException(ResponseCode.E0106);
        }

        dynamicContext.setGroupBuyTeamEntity(teamEntity);
        return next(requestParameter, dynamicContext);
    }

    private boolean validOrderAndTeam(GroupBuyOrderEntity orderEntity, GroupBuyTeamEntity teamEntity) {
        return orderEntity != null
                && orderEntity.getTeamId() != null
                && teamEntity != null
                && teamEntity.getTeamId() != null;
    }
}
