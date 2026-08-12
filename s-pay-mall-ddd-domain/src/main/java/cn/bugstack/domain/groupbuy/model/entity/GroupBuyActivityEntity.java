package cn.bugstack.domain.groupbuy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyActivityEntity {

    private Long id;

    private Long activityId;

    private String activityName;

    private Long productId;

    private String discountId;

    private Integer groupType;

    private Integer takeLimitCount;

    private Integer targetCount;

    private Integer validTime;

    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String tagId;

    private String tagScope;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
