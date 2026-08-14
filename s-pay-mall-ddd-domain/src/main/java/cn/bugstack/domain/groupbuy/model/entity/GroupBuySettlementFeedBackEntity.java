package cn.bugstack.domain.groupbuy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 结算-反馈实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuySettlementFeedBackEntity {

    private String teamId;
    private Long activityId;
    private Integer targetCount;
    private Integer completeCount;
    private Integer lockCount;
    private Integer status;
    private LocalDateTime validStartTime;
    private LocalDateTime validEndTime;
    private String notifyUrl;

    /** 本次是否因结算达成成团 */
    private boolean complete;

}
