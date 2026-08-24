package cn.bugstack.domain.groupbuy.service.refund.filter;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyOrderEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderBehaviorEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderCommandEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTeamEntity;
import cn.bugstack.domain.groupbuy.model.valobj.GroupBuyOrderStatusEnumVO;
import cn.bugstack.domain.groupbuy.service.refund.factory.GroupBuyRefundOrderRuleFilterFactory;
import cn.bugstack.types.design.framework.link.multilink.handler.ILogicHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RefundOrderNodeFilter implements ILogicHandler<GroupBuyRefundOrderCommandEntity, GroupBuyRefundOrderRuleFilterFactory.DynamicContext, GroupBuyRefundOrderBehaviorEntity> {

    @Override
    public GroupBuyRefundOrderBehaviorEntity apply(GroupBuyRefundOrderCommandEntity requestParameter, GroupBuyRefundOrderRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        GroupBuyOrderEntity groupBuyOrderEntity = dynamicContext.getGroupBuyOrderEntity();
        GroupBuyTeamEntity groupBuyTeamEntity = dynamicContext.getGroupBuyTeamEntity();
        GroupBuyOrderStatusEnumVO orderStatus = GroupBuyOrderStatusEnumVO.valueOf(groupBuyOrderEntity.getStatus());

        if (GroupBuyOrderStatusEnumVO.PAID.equals(orderStatus)) {
            boolean teamCompleted = groupBuyTeamEntity.getCompleteCount() != null
                    && groupBuyTeamEntity.getTargetCount() != null
                    && groupBuyTeamEntity.getCompleteCount() >= groupBuyTeamEntity.getTargetCount();
            String strategyName = teamCompleted ? "paidTeamRefundStrategy" : "paidRefundStrategy";
            String message = teamCompleted ? "已支付已成团，进入成团退款策略" : "已支付未成团，进入退款策略";
            dynamicContext.setMessage(message);
            return GroupBuyRefundOrderBehaviorEntity.builder()
                    .userId(groupBuyOrderEntity.getUserId())
                    .orderId(groupBuyOrderEntity.getOrderId())
                    .teamId(groupBuyOrderEntity.getTeamId())
                    .activityId(groupBuyOrderEntity.getActivityId())
                    .success(true)
                    .strategyName(strategyName)
                    .message(message)
                    .build();
        }

        if (GroupBuyOrderStatusEnumVO.LOCKED.equals(orderStatus)) {
            String message = "未支付未成团，进入退款策略";
            dynamicContext.setMessage(message);
            return GroupBuyRefundOrderBehaviorEntity.builder()
                    .userId(groupBuyOrderEntity.getUserId())
                    .orderId(groupBuyOrderEntity.getOrderId())
                    .teamId(groupBuyOrderEntity.getTeamId())
                    .activityId(groupBuyOrderEntity.getActivityId())
                    .success(true)
                    .strategyName("unpaidNotTeamRefundStrategy")
                    .message(message)
                    .build();
        }

        String message = "当前订单状态无需退单处理: " + orderStatus.getInfo();
        dynamicContext.setMessage(message);
        return GroupBuyRefundOrderBehaviorEntity.builder()
                .userId(groupBuyOrderEntity.getUserId())
                .orderId(groupBuyOrderEntity.getOrderId())
                .teamId(groupBuyOrderEntity.getTeamId())
                .activityId(groupBuyOrderEntity.getActivityId())
                .success(true)
                .message(message)
                .build();
    }
}
