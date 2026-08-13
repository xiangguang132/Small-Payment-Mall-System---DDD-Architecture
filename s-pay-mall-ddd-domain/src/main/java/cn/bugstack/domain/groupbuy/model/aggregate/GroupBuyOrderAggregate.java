package cn.bugstack.domain.groupbuy.model.aggregate;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyOrderAggregate {

    private GroupBuyTrialResult trialResult;
    private String userId;
    private String teamId;
    private String source;
    private String channel;
    private String outTradeNo;
    private String notifyUrl;

}
