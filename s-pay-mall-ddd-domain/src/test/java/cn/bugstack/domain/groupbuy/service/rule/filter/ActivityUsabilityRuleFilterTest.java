package cn.bugstack.domain.groupbuy.service.rule.filter;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRuleCommandEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRuleFilterFeedBackEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyActivityRepository;
import cn.bugstack.domain.groupbuy.service.rule.factory.GroupBuyRuleFilterFactory;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ActivityUsabilityRuleFilterTest {

    @Mock
    private IGroupBuyActivityRepository activityRepository;

    @InjectMocks
    private ActivityUsabilityRuleFilter activityUsabilityRuleFilter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldRejectWhenActivityIdIsNull() throws Exception {
        GroupBuyRuleCommandEntity command = GroupBuyRuleCommandEntity.builder()
                .userId("U1")
                .build();

        try {
            activityUsabilityRuleFilter.apply(command, newContext());
            fail("should throw AppException");
        } catch (AppException e) {
            assertEquals(ResponseCode.ILLEGAL_PARAMETER.getCode(), e.getCode());
        }
    }

    @Test
    public void shouldRejectWhenActivityNotFound() throws Exception {
        when(activityRepository.queryGroupBuyActivityByActivityId(100L))
                .thenReturn(null);

        try {
            activityUsabilityRuleFilter.apply(command(), newContext());
            fail("should throw AppException");
        } catch (AppException e) {
            assertEquals(ResponseCode.E0101.getCode(), e.getCode());
        }
    }

    @Test
    public void shouldRejectWhenActivityNotEffective() throws Exception {
        when(activityRepository.queryGroupBuyActivityByActivityId(100L))
                .thenReturn(effectiveActivity(0));

        try {
            activityUsabilityRuleFilter.apply(command(), newContext());
            fail("should throw AppException");
        } catch (AppException e) {
            assertEquals(ResponseCode.E0101.getCode(), e.getCode());
        }
    }

    @Test
    public void shouldRejectWhenBeforeStartTime() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        when(activityRepository.queryGroupBuyActivityByActivityId(100L))
                .thenReturn(activity(1, now.plusHours(1), now.plusHours(2)));

        try {
            activityUsabilityRuleFilter.apply(command(), newContext());
            fail("should throw AppException");
        } catch (AppException e) {
            assertEquals(ResponseCode.E0102.getCode(), e.getCode());
        }
    }

    @Test
    public void shouldRejectWhenAfterEndTime() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        when(activityRepository.queryGroupBuyActivityByActivityId(100L))
                .thenReturn(activity(1, now.minusHours(2), now.minusHours(1)));

        try {
            activityUsabilityRuleFilter.apply(command(), newContext());
            fail("should throw AppException");
        } catch (AppException e) {
            assertEquals(ResponseCode.E0102.getCode(), e.getCode());
        }
    }

    @Test
    public void shouldWriteActivityAndContinueWhenEffectiveAndInTime() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        GroupBuyActivityEntity activity = activity(1, now.minusHours(1), now.plusHours(1));
        when(activityRepository.queryGroupBuyActivityByActivityId(100L))
                .thenReturn(activity);
        GroupBuyRuleFilterFactory.DynamicContext context = newContext();

        GroupBuyRuleFilterFeedBackEntity result = activityUsabilityRuleFilter.apply(command(), context);

        assertNull(result);
        assertSame(activity, context.getActivity());
    }

    private GroupBuyRuleCommandEntity command() {
        return GroupBuyRuleCommandEntity.builder()
                .userId("U1")
                .activityId(100L)
                .outTradeNo("B1")
                .build();
    }

    private GroupBuyRuleFilterFactory.DynamicContext newContext() {
        return new GroupBuyRuleFilterFactory.DynamicContext();
    }

    private GroupBuyActivityEntity effectiveActivity(Integer status) {
        LocalDateTime now = LocalDateTime.now();
        return activity(status, now.minusHours(1), now.plusHours(1));
    }

    private GroupBuyActivityEntity activity(Integer status,
                                            LocalDateTime startTime,
                                            LocalDateTime endTime) {
        return GroupBuyActivityEntity.builder()
                .activityId(100L)
                .status(status)
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }
}
