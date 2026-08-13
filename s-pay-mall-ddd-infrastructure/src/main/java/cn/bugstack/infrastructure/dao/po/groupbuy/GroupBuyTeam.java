package cn.bugstack.infrastructure.dao.po.groupbuy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyTeam {

    private Long id;

    private String teamId;

    private Long activityId;

    private String initiatorUserId;

    private Integer targetCount;

    private Integer completeCount;

    private Integer lockCount;

    private Integer status;

    private LocalDateTime validStartTime;

    private LocalDateTime validEndTime;

    private String notifyUrl;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
