package cn.bugstack.domain.groupbuy.service.refund.filter;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyOrderEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderBehaviorEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderCommandEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTeamEntity;
import cn.bugstack.domain.groupbuy.model.valobj.GroupBuyOrderStatusEnumVO;
import cn.bugstack.domain.groupbuy.service.refund.factory.GroupBuyRefundOrderRuleFilterFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RefundOrderNodeFilterTest {

    private final RefundOrderNodeFilter refundOrderNodeFilter = new RefundOrderNodeFilter();

    @Test
    public void shouldRouteToUnpaidNotTeamRefundStrategyWhenOrderIsLocked() throws Exception {
        GroupBuyRefundOrderRuleFilterFactory.DynamicContext context = newContext(
                order(GroupBuyOrderStatusEnumVO.LOCKED.getCode()),
                team(1, 3)
        );

        GroupBuyRefundOrderBehaviorEntity result = refundOrderNodeFilter.apply(command(), context);

        assertEquals("unpaidNotTeamRefundStrategy", result.getStrategyName());
        assertEquals("未支付未成团，进入退款策略", result.getMessage());
    }

    @Test
    public void shouldRouteToPaidRefundStrategyWhenOrderPaidAndTeamNotCompleted() throws Exception {
        GroupBuyRefundOrderRuleFilterFactory.DynamicContext context = newContext(
                order(GroupBuyOrderStatusEnumVO.PAID.getCode()),
                team(1, 3)
        );

        GroupBuyRefundOrderBehaviorEntity result = refundOrderNodeFilter.apply(command(), context);

        assertEquals("paidRefundStrategy", result.getStrategyName());
        assertEquals("已支付未成团，进入退款策略", result.getMessage());
    }

    @Test
    public void shouldRouteToPaidTeamRefundStrategyWhenOrderPaidAndTeamCompleted() throws Exception {
        GroupBuyRefundOrderRuleFilterFactory.DynamicContext context = newContext(
                order(GroupBuyOrderStatusEnumVO.PAID.getCode()),
                team(3, 3)
        );

        GroupBuyRefundOrderBehaviorEntity result = refundOrderNodeFilter.apply(command(), context);

        assertEquals("paidTeamRefundStrategy", result.getStrategyName());
        assertEquals("已支付已成团，进入成团退款策略", result.getMessage());
    }

    @Test
    public void shouldReturnNoRouteResultWhenOrderCompleted() throws Exception {
        GroupBuyRefundOrderRuleFilterFactory.DynamicContext context = newContext(
                order(GroupBuyOrderStatusEnumVO.COMPLETE.getCode()),
                team(3, 3)
        );

        GroupBuyRefundOrderBehaviorEntity result = refundOrderNodeFilter.apply(command(), context);

        assertNull(result.getStrategyName());
        assertEquals("当前订单状态无需退单处理: 已完成", result.getMessage());
    }

    private GroupBuyRefundOrderCommandEntity command() {
        return GroupBuyRefundOrderCommandEntity.builder()
                .userId("U1")
                .orderId("O1")
                .teamId("T1")
                .activityId(100L)
                .outTradeNo("B1")
                .build();
    }

    private GroupBuyRefundOrderRuleFilterFactory.DynamicContext newContext(GroupBuyOrderEntity orderEntity,
                                                                          GroupBuyTeamEntity teamEntity) {
        return GroupBuyRefundOrderRuleFilterFactory.DynamicContext.builder()
                .groupBuyOrderEntity(orderEntity)
                .groupBuyTeamEntity(teamEntity)
                .build();
    }

    private GroupBuyOrderEntity order(Integer status) {
        return GroupBuyOrderEntity.builder()
                .userId("U1")
                .orderId("O1")
                .teamId("T1")
                .activityId(100L)
                .status(status)
                .build();
    }

    private GroupBuyTeamEntity team(Integer completeCount, Integer targetCount) {
        return GroupBuyTeamEntity.builder()
                .teamId("T1")
                .activityId(100L)
                .completeCount(completeCount)
                .targetCount(targetCount)
                .build();
    }
}
