package cn.bugstack.domain.groupbuy.service.refund;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderBehaviorEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderCommandEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderEntity;
import cn.bugstack.domain.groupbuy.service.refund.business.IGroupBuyRefundOrderStrategy;
import cn.bugstack.domain.groupbuy.service.refund.factory.GroupBuyRefundOrderRuleFilterFactory;
import cn.bugstack.types.design.framework.link.multilink.chain.BusinessLinkedList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Service
public class GroupBuyRefundOrderService implements IGroupBuyRefundOrderService {

    @Resource(name = "groupBuyRefundOrderRuleFilterFactory")
    private BusinessLinkedList<GroupBuyRefundOrderCommandEntity, GroupBuyRefundOrderRuleFilterFactory.DynamicContext, GroupBuyRefundOrderBehaviorEntity> groupBuyRefundOrderRuleFilter;

    @Resource
    private Map<String, IGroupBuyRefundOrderStrategy> refundGroupBuyOrderStrategyMap;

    /**
     * 拼团退单
     * @param groupBuyRefundOrderCommandEntity
     * @return
     */
    @Override
    public GroupBuyRefundOrderBehaviorEntity refundGroupBuyOrder(GroupBuyRefundOrderCommandEntity groupBuyRefundOrderCommandEntity) {
        GroupBuyRefundOrderRuleFilterFactory.DynamicContext dynamicContext = GroupBuyRefundOrderRuleFilterFactory.DynamicContext.builder().build();
        GroupBuyRefundOrderBehaviorEntity behaviorEntity;
        try {
            behaviorEntity = groupBuyRefundOrderRuleFilter.apply(groupBuyRefundOrderCommandEntity, dynamicContext);
        } catch (Exception e) {
            return GroupBuyRefundOrderBehaviorEntity.builder()
                    .userId(groupBuyRefundOrderCommandEntity.getUserId())
                    .teamId(groupBuyRefundOrderCommandEntity.getTeamId())
                    .orderId(groupBuyRefundOrderCommandEntity.getOrderId())
                    .activityId(groupBuyRefundOrderCommandEntity.getActivityId())
                    .success(false)
                    .message("拼团退单过滤链执行失败")
                    .build();
        }
        if (behaviorEntity == null) {
            return GroupBuyRefundOrderBehaviorEntity.builder()
                    .userId(groupBuyRefundOrderCommandEntity.getUserId())
                    .teamId(groupBuyRefundOrderCommandEntity.getTeamId())
                    .orderId(groupBuyRefundOrderCommandEntity.getOrderId())
                    .activityId(groupBuyRefundOrderCommandEntity.getActivityId())
                    .success(false)
                    .message(dynamicContext.getMessage())
                    .build();
        }
        String strategyName = behaviorEntity.getStrategyName();
        if (strategyName == null || strategyName.isEmpty()) {
            return behaviorEntity;
        }
        IGroupBuyRefundOrderStrategy strategy = refundGroupBuyOrderStrategyMap.get(strategyName);
        if (strategy == null) {
            log.warn("未找到拼团退单策略 strategyName:{}", strategyName);
            return GroupBuyRefundOrderBehaviorEntity.builder()
                    .userId(groupBuyRefundOrderCommandEntity.getUserId())
                    .teamId(groupBuyRefundOrderCommandEntity.getTeamId())
                    .orderId(groupBuyRefundOrderCommandEntity.getOrderId())
                    .activityId(groupBuyRefundOrderCommandEntity.getActivityId())
                    .success(false)
                    .message("未找到退单策略")
                    .build();
        }
        GroupBuyRefundOrderEntity refundOrderEntity = GroupBuyRefundOrderEntity.builder()
                .userId(behaviorEntity.getUserId())
                .teamId(behaviorEntity.getTeamId())
                .activityId(behaviorEntity.getActivityId())
                .orderId(behaviorEntity.getOrderId())
                .build();
        strategy.refundGroupBuyOrder(refundOrderEntity);
        behaviorEntity.setStrategyName(null);
        return behaviorEntity;
    }
}
