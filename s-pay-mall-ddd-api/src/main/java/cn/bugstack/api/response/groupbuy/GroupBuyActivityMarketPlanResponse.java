package cn.bugstack.api.response.groupbuy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyActivityMarketPlanResponse {

    private Long activityId;

    private String activityName;

    private Long productId;

    private String discountId;

    private String discountName;

    private String marketPlan;

//    private Integer groupType;

//    private Integer targetCount;

//    private Integer validTime;

//    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

//    private LocalDateTime createTime;

}
