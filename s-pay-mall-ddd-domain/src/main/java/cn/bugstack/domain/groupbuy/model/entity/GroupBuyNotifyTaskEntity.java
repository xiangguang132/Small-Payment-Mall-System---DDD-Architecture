package cn.bugstack.domain.groupbuy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyNotifyTaskEntity {

    private Long activityId;
    private String teamId;
    private String notifyType;    // MQ / HTTP
    private String notifyMQ;      // topic.team_success
    private String notifyUrl;
    private Integer notifyCount;
    private Integer notifyStatus; // 0初始 1完成 2重试 3失败
    private String parameterJson; // {"teamId":"xxx","outTradeNoList":[".."]}
    private String uuid;          // teamId_trade_settlement_outTradeNo（幂等）

    // 分布式锁 key（本站单机可预留；多实例部署时用）
    public String lockKey() {
        return "group_buy_notify_job_lock_" + this.uuid;
    }

}
