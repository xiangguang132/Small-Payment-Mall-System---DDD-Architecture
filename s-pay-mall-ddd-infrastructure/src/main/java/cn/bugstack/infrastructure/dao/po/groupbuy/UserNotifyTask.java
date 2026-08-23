package cn.bugstack.infrastructure.dao.po.groupbuy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotifyTask {

    private Long id;
    private String userId;
    private String teamId;
    private Long activityId;
    private String notifyType;   // TEAM_SUCCESS
    private String title;
    private String content;
    private Integer isRead;      // 0未读 1已读
    private Date createTime;
    private Date updateTime;

}
