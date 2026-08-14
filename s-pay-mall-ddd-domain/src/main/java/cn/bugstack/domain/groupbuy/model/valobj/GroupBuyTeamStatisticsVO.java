package cn.bugstack.domain.groupbuy.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 团队结算统计值对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupBuyTeamStatisticsVO {

    private String teamId;
    private Long activityId;
    private Integer targetCount;
    private Integer completeCount;
    private Integer lockCount;
    private Integer status;
    private LocalDateTime validStartTime;
    private LocalDateTime validEndTime;

    private Integer paidOrderCount;
    private BigDecimal totalOriginalAmount;
    private BigDecimal totalDeductionAmount;
    private BigDecimal totalPayAmount;


}
