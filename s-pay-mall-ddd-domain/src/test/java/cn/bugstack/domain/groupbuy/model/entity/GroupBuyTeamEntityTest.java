package cn.bugstack.domain.groupbuy.model.entity;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class GroupBuyTeamEntityTest {

    @Test
    public void builderShouldRetainAllGroupBuyTeamFields() {
        LocalDateTime validStartTime = LocalDateTime.of(2026, 8, 13, 10, 0);
        LocalDateTime validEndTime = LocalDateTime.of(2026, 8, 13, 10, 15);

        GroupBuyTeamEntity team = GroupBuyTeamEntity.builder()
                .id(1L)
                .teamId("T1")
                .activityId(100L)
                .initiatorUserId("U1")
                .targetCount(3)
                .completeCount(1)
                .lockCount(2)
                .status(0)
                .validStartTime(validStartTime)
                .validEndTime(validEndTime)
                .notifyUrl("http://localhost/callback")
                .createTime(validStartTime)
                .updateTime(validStartTime)
                .build();

        assertEquals(Long.valueOf(1L), team.getId());
        assertEquals("T1", team.getTeamId());
        assertEquals(Long.valueOf(100L), team.getActivityId());
        assertEquals("U1", team.getInitiatorUserId());
        assertEquals(Integer.valueOf(3), team.getTargetCount());
        assertEquals(Integer.valueOf(1), team.getCompleteCount());
        assertEquals(Integer.valueOf(2), team.getLockCount());
        assertEquals(Integer.valueOf(0), team.getStatus());
        assertEquals(validStartTime, team.getValidStartTime());
        assertEquals(validEndTime, team.getValidEndTime());
        assertEquals("http://localhost/callback", team.getNotifyUrl());
        assertEquals(validStartTime, team.getCreateTime());
        assertEquals(validStartTime, team.getUpdateTime());
    }
}
