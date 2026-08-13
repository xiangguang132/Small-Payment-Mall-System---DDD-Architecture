package cn.bugstack.domain.groupbuy.service.rule.filter;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRuleCommandEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRuleFilterFeedBackEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyRepository;
import cn.bugstack.domain.groupbuy.service.rule.factory.GroupBuyRuleFilterFactory;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class UserTakeLimitRuleFilterTest {

    @Mock
    private IGroupBuyRepository groupBuyRepository;

    @InjectMocks
    private UserTakeLimitRuleFilter userTakeLimitRuleFilter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldRejectWhenUserTakeLimitReached() throws Exception {
        GroupBuyRuleFilterFactory.DynamicContext context = newContext(3);
        when(groupBuyRepository.countUserGroupBuyOrders("U1", 100L))
                .thenReturn(3);

        try {
            userTakeLimitRuleFilter.apply(command(), context);
            fail("should throw AppException");
        } catch (AppException e) {
            assertEquals(ResponseCode.E0103.getCode(), e.getCode());
        }
    }

    @Test
    public void shouldReturnCountWhenUserTakeLimitNotReached() throws Exception {
        GroupBuyRuleFilterFactory.DynamicContext context = newContext(3);
        when(groupBuyRepository.countUserGroupBuyOrders("U1", 100L))
                .thenReturn(2);

        GroupBuyRuleFilterFeedBackEntity result = userTakeLimitRuleFilter.apply(command(), context);

        assertEquals(Integer.valueOf(2), result.getUserTakeOrderCount());
    }

    private GroupBuyRuleCommandEntity command() {
        return GroupBuyRuleCommandEntity.builder()
                .userId("U1")
                .activityId(100L)
                .outTradeNo("B1")
                .build();
    }

    private GroupBuyRuleFilterFactory.DynamicContext newContext(Integer takeLimitCount) {
        GroupBuyActivityEntity activity = GroupBuyActivityEntity.builder()
                .activityId(100L)
                .takeLimitCount(takeLimitCount)
                .build();
        return GroupBuyRuleFilterFactory.DynamicContext.builder()
                .activity(activity)
                .build();
    }
}
