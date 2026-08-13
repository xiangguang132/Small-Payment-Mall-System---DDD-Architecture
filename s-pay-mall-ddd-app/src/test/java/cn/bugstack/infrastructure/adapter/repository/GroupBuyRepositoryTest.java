package cn.bugstack.infrastructure.adapter.repository;

import cn.bugstack.domain.groupbuy.model.aggregate.GroupBuyOrderAggregate;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyOrderEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialResult;
import cn.bugstack.infrastructure.dao.IGroupBuyOrderDao;
import cn.bugstack.infrastructure.dao.IGroupBuyTeamDao;
import cn.bugstack.infrastructure.dao.po.groupbuy.GroupBuyOrder;
import cn.bugstack.infrastructure.dao.po.groupbuy.GroupBuyTeam;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GroupBuyRepositoryTest {

    @Mock
    private IGroupBuyTeamDao groupBuyTeamDao;

    @Mock
    private IGroupBuyOrderDao groupBuyOrderDao;

    @InjectMocks
    private GroupBuyRepository groupBuyRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldCreateNewTeamAndOrderWhenTeamIdBlank() {
        GroupBuyOrderEntity result = groupBuyRepository.lockGroupBuyOrder(
                aggregate(null, "B1")
        );

        assertNotNull(result.getTeamId());
        assertNotNull(result.getOrderId());
        assertEquals("U1", result.getUserId());
        assertEquals(Long.valueOf(100L), result.getActivityId());
        assertEquals(new BigDecimal("90.00"), result.getPayAmount());

        ArgumentCaptor<GroupBuyTeam> teamCaptor = ArgumentCaptor.forClass(GroupBuyTeam.class);
        verify(groupBuyTeamDao).insert(teamCaptor.capture());
        assertEquals(result.getTeamId(), teamCaptor.getValue().getTeamId());
        assertEquals(Integer.valueOf(1), teamCaptor.getValue().getLockCount());

        ArgumentCaptor<GroupBuyOrder> orderCaptor = ArgumentCaptor.forClass(GroupBuyOrder.class);
        verify(groupBuyOrderDao).insert(orderCaptor.capture());
        assertEquals("B1", orderCaptor.getValue().getBizId());
        assertEquals(result.getOrderId(), orderCaptor.getValue().getOrderId());
    }

    @Test
    public void shouldThrowWhenJoinFullTeam() {
        when(groupBuyTeamDao.updateAddLockCount("T1")).thenReturn(0);

        try {
            groupBuyRepository.lockGroupBuyOrder(aggregate("T1", "B2"));
            fail("should throw AppException");
        } catch (AppException e) {
            assertEquals(ResponseCode.E0005.getCode(), e.getCode());
        }

        verify(groupBuyOrderDao, never()).insert(any());
    }

    private GroupBuyOrderAggregate aggregate(String teamId, String outTradeNo) {
        return GroupBuyOrderAggregate.builder()
                .userId("U1")
                .teamId(teamId)
                .outTradeNo(outTradeNo)
                .source("s01")
                .channel("c01")
                .notifyUrl("http://localhost/callback")
                .trialResult(GroupBuyTrialResult.builder()
                        .activityId(100L)
                        .productId(200L)
                        .productName("Product")
                        .targetCount(3)
                        .validTime(15)
                        .originalPrice(new BigDecimal("99.90"))
                        .deductionPrice(new BigDecimal("9.90"))
                        .payPrice(new BigDecimal("90.00"))
                        .build())
                .build();
    }
}
