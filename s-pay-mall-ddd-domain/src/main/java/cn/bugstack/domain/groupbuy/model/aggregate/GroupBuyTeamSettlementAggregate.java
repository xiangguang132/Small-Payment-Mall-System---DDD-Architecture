package cn.bugstack.domain.groupbuy.model.aggregate;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuySettlementCommandEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTeamEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 结算聚合
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupBuyTeamSettlementAggregate {

    private GroupBuySettlementCommandEntity settlementCommand;
    private GroupBuyTeamEntity groupBuyTeamEntity;

}
