package cn.bugstack.domain.groupbuy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotifyEntity {

    private String userId;
    private String teamId;
    private Long activityId;
    private String notifyType;   // TEAM_SUCCESS
    private String title;
    private String content;
    private Integer isRead;      // 0未读 1已读

}
