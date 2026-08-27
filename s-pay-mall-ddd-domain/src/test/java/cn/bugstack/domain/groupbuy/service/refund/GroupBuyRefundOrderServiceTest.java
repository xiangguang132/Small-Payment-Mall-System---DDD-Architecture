package cn.bugstack.domain.groupbuy.service.refund;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderBehaviorEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderCommandEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderEntity;
import cn.bugstack.domain.groupbuy.service.refund.business.IGroupBuyRefundOrderStrategy;
import cn.bugstack.domain.groupbuy.service.refund.factory.GroupBuyRefundOrderRuleFilterFactory;
import cn.bugstack.types.design.framework.link.multilink.chain.BusinessLinkedList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GroupBuyRefundOrderServiceTest {

    @Mock
    private BusinessLinkedList<GroupBuyRefundOrderCommandEntity,
            GroupBuyRefundOrderRuleFilterFactory.DynamicContext,
            GroupBuyRefundOrderBehaviorEntity> groupBuyRefundOrderRuleFilter;

    @Mock
    private Map<String, IGroupBuyRefundOrderStrategy> refundGroupBuyOrderStrategyMap;

    @Mock
    private IGroupBuyRefundOrderStrategy refundStrategy;

    @InjectMocks
    private GroupBuyRefundOrderService groupBuyRefundOrderService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldExecuteStrategyWhenRouteExists() throws Exception {
        GroupBuyRefundOrderBehaviorEntity behaviorEntity = GroupBuyRefundOrderBehaviorEntity.builder()
                .userId("U1")
                .orderId("O1")
                .teamId("T1")
                .activityId(100L)
                .outTradeNo("B1")
                .strategyName("paidRefundStrategy")
                .message("已支付未成团，进入退款策略")
                .success(true)
                .build();

        when(groupBuyRefundOrderRuleFilter.apply(any(), any())).thenReturn(behaviorEntity);
        when(refundGroupBuyOrderStrategyMap.get("paidRefundStrategy")).thenReturn(refundStrategy);

        GroupBuyRefundOrderBehaviorEntity result = groupBuyRefundOrderService.refundGroupBuyOrder(command());

        assertNull(result.getStrategyName());
        assertEquals("已支付未成团，进入退款策略", result.getMessage());
        verify(refundStrategy).refundGroupBuyOrder(GroupBuyRefundOrderEntity.builder()
                .userId("U1")
                .orderId("O1")
                .teamId("T1")
                .activityId(100L)
                .outTradeNo("B1")
                .build());
    }

    @Test
    public void shouldReturnOriginalBehaviorWhenNoRouteNeeded() throws Exception {
        GroupBuyRefundOrderBehaviorEntity behaviorEntity = GroupBuyRefundOrderBehaviorEntity.builder()
                .userId("U1")
                .orderId("O1")
                .teamId("T1")
                .activityId(100L)
                .message("当前订单状态无需退单处理: 已完成")
                .success(true)
                .build();

        when(groupBuyRefundOrderRuleFilter.apply(any(), any())).thenReturn(behaviorEntity);

        GroupBuyRefundOrderBehaviorEntity result = groupBuyRefundOrderService.refundGroupBuyOrder(command());

        assertEquals("当前订单状态无需退单处理: 已完成", result.getMessage());
    }

    @Test
    public void shouldReturnFailureWhenStrategyMissing() throws Exception {
        GroupBuyRefundOrderBehaviorEntity behaviorEntity = GroupBuyRefundOrderBehaviorEntity.builder()
                .userId("U1")
                .orderId("O1")
                .teamId("T1")
                .activityId(100L)
                .strategyName("paidRefundStrategy")
                .message("已支付未成团，进入退款策略")
                .success(true)
                .build();

        when(groupBuyRefundOrderRuleFilter.apply(any(), any())).thenReturn(behaviorEntity);
        when(refundGroupBuyOrderStrategyMap.get("paidRefundStrategy")).thenReturn(null);

        GroupBuyRefundOrderBehaviorEntity result = groupBuyRefundOrderService.refundGroupBuyOrder(command());

        assertEquals("未找到退单策略", result.getMessage());
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
}
