package cn.bugstack.domain.groupbuy.service.trial.node;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialRequest;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialResult;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyActivityRepository;
import cn.bugstack.domain.groupbuy.service.trial.factory.DefaultActivityStrategyFactory;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SwitchNodeTest {

    @Test
    public void shouldRejectWhenDowngradeSwitchEnabled() {
        IGroupBuyActivityRepository repository = mock(IGroupBuyActivityRepository.class);
        when(repository.downgradeSwitch()).thenReturn(true);

        SwitchNode node = new SwitchNode();
        ReflectionTestUtils.setField(node, "activityRepository", repository);

        try {
            node.doApply(request(), new DefaultActivityStrategyFactory.DynamicContext());
            fail("should reject downgrade");
        } catch (AppException e) {
            assertEquals(ResponseCode.E0003.getCode(), e.getCode());
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void shouldRejectWhenUserNotInCutRange() {
        IGroupBuyActivityRepository repository = mock(IGroupBuyActivityRepository.class);
        when(repository.downgradeSwitch()).thenReturn(false);
        when(repository.cutRange("10001")).thenReturn(false);

        SwitchNode node = new SwitchNode();
        ReflectionTestUtils.setField(node, "activityRepository", repository);

        try {
            node.doApply(request(), new DefaultActivityStrategyFactory.DynamicContext());
            fail("should reject cut range");
        } catch (AppException e) {
            assertEquals(ResponseCode.E0004.getCode(), e.getCode());
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void shouldRouteWhenDowngradeAndCutRangeAllow() throws Exception {
        IGroupBuyActivityRepository repository = mock(IGroupBuyActivityRepository.class);
        when(repository.downgradeSwitch()).thenReturn(false);
        when(repository.cutRange("10001")).thenReturn(true);

        MarketNode marketNode = mock(MarketNode.class);
        GroupBuyTrialResult expected = new GroupBuyTrialResult();
        when(marketNode.apply(any(), any())).thenReturn(expected);

        SwitchNode node = new SwitchNode();
        ReflectionTestUtils.setField(node, "activityRepository", repository);
        ReflectionTestUtils.setField(node, "marketNode", marketNode);

        GroupBuyTrialResult result = node.doApply(
                request(),
                new DefaultActivityStrategyFactory.DynamicContext()
        );

        assertSame(expected, result);
        verify(marketNode).apply(any(), any());
    }

    private GroupBuyTrialRequest request() {
        return GroupBuyTrialRequest.builder()
                .userId("10001")
                .activityId(10001L)
                .build();
    }
}
