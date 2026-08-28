package cn.bugstack.domain.groupbuy.model.aggregate;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    /** 锁单时使用的优惠券ID列表（用于核销） */
    private List<String> couponIds;

}
