package cn.bugstack.domain.groupbuy.service.order;

import cn.bugstack.domain.groupbuy.model.aggregate.GroupBuyOrderAggregate;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyOrderEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialResult;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyRepository;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GroupBuyOrderServiceTest {

    @Mock
    private IGroupBuyRepository groupBuyRepository;

    @InjectMocks
    private GroupBuyOrderService groupBuyOrderService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldReturnExistingOrderWhenSameUserAndBizIdAlreadyLocked() {
        GroupBuyOrderEntity existing = GroupBuyOrderEntity.builder()
                .userId("U1")
                .orderId("O1")
                .build();
        when(groupBuyRepository.queryGroupBuyOrderByBizId("U1", "B1"))
                .thenReturn(existing);

        GroupBuyOrderEntity result = groupBuyOrderService.lockGroupBuyOrder(
                aggregate("U1", "B1", 3)
        );

        assertEquals("O1", result.getOrderId());
        verify(groupBuyRepository, never()).lockGroupBuyOrder(any());
    }

    @Test
    public void shouldRejectWhenUserTakeLimitExceeded() {
        when(groupBuyRepository.queryGroupBuyOrderByBizId("U1", "B1"))
                .thenReturn(null);
        when(groupBuyRepository.countUserGroupBuyOrders("U1", 100L))
                .thenReturn(3);

        try {
            groupBuyOrderService.lockGroupBuyOrder(aggregate("U1", "B1", 3));
            fail("should throw AppException");
        } catch (AppException e) {
            assertEquals(ResponseCode.E0103.getCode(), e.getCode());
        }

        verify(groupBuyRepository, never()).lockGroupBuyOrder(any());
    }

    @Test
    public void shouldLockWhenNoExistingOrderAndUnderLimit() {
        GroupBuyOrderEntity created = GroupBuyOrderEntity.builder()
                .userId("U1")
                .orderId("O2")
                .build();
        when(groupBuyRepository.queryGroupBuyOrderByBizId("U1", "B1"))
                .thenReturn(null);
        when(groupBuyRepository.countUserGroupBuyOrders("U1", 100L))
                .thenReturn(1);
        when(groupBuyRepository.lockGroupBuyOrder(any(GroupBuyOrderAggregate.class)))
                .thenReturn(created);

        GroupBuyOrderEntity result = groupBuyOrderService.lockGroupBuyOrder(
                aggregate("U1", "B1", 3)
        );

        assertEquals("O2", result.getOrderId());
        verify(groupBuyRepository).lockGroupBuyOrder(any(GroupBuyOrderAggregate.class));
    }

    private GroupBuyOrderAggregate aggregate(String userId, String outTradeNo, Integer takeLimitCount) {
        return GroupBuyOrderAggregate.builder()
                .userId(userId)
                .outTradeNo(outTradeNo)
                .trialResult(GroupBuyTrialResult.builder()
                        .activityId(100L)
                        .takeLimitCount(takeLimitCount)
                        .build())
                .build();
    }
}
