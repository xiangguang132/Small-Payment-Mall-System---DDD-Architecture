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
public class GroupBuyNotifyTask {

    private Long id;
    private String teamId;
    private Long activityId;
    private String notifyMq;      // topic.team_success
    private Integer notifyStatus; // 0待发 1成功 2重试 3失败
    private Integer notifyCount;  // 重试次数
    private String parameterJson; // {"teamId":"..","outTradeNoList":[..]}
    private Date createTime;
    private Date updateTime;

}
