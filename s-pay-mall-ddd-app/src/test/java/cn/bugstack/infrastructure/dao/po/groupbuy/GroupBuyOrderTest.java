package cn.bugstack.infrastructure.dao.po.groupbuy;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class GroupBuyOrderTest {

    @Test
    public void builderShouldRetainAllGroupBuyOrderFields() {
        LocalDateTime createTime = LocalDateTime.of(2026, 8, 13, 10, 0);

        GroupBuyOrder order = GroupBuyOrder.builder()
                .id(1L)
                .orderId("O1")
                .userId("U1")
                .teamId("T1")
                .activityId(100L)
                .productId(200L)
                .productName("Product")
                .quantity(2)
                .source("s")
                .channel("c")
                .originalAmount(new BigDecimal("99.90"))
                .deductionAmount(new BigDecimal("9.90"))
                .payAmount(new BigDecimal("90.00"))
                .status(0)
                .bizId("B1")
                .createTime(createTime)
                .updateTime(createTime)
                .build();

        assertEquals(Long.valueOf(1L), order.getId());
        assertEquals("O1", order.getOrderId());
        assertEquals("U1", order.getUserId());
        assertEquals("T1", order.getTeamId());
        assertEquals(Long.valueOf(100L), order.getActivityId());
        assertEquals(Long.valueOf(200L), order.getProductId());
        assertEquals("Product", order.getProductName());
        assertEquals(Integer.valueOf(2), order.getQuantity());
        assertEquals("s", order.getSource());
        assertEquals("c", order.getChannel());
        assertEquals(new BigDecimal("99.90"), order.getOriginalAmount());
        assertEquals(new BigDecimal("9.90"), order.getDeductionAmount());
        assertEquals(new BigDecimal("90.00"), order.getPayAmount());
        assertEquals(Integer.valueOf(0), order.getStatus());
        assertEquals("B1", order.getBizId());
        assertEquals(createTime, order.getCreateTime());
        assertEquals(createTime, order.getUpdateTime());
    }
}
