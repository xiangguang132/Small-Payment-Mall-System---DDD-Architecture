package cn.bugstack.domain.groupbuy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 退单-恢复实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyRefundRestoreEntity {

    private String refundType;    // 策略 bean 名：paidRefundStrategy / unpaidNotTeamRefundStrategy / paidTeamRefundStrategy
    private String userId;
    private String teamId;
    private Long activityId;
    private String orderId;
    private String outTradeNo;

}
