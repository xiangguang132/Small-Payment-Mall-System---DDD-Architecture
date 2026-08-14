package cn.bugstack.domain.groupbuy.service.settlement.factory;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyOrderEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuySettlementCommandEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuySettlementFeedBackEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTeamEntity;
import cn.bugstack.domain.groupbuy.service.settlement.filter.EndRuleFilter;
import cn.bugstack.domain.groupbuy.service.settlement.filter.OutTradeNoRuleFilter;
import cn.bugstack.domain.groupbuy.service.settlement.filter.SettableRuleFilter;
import cn.bugstack.types.design.framework.link.multilink.LinkArmory;
import cn.bugstack.types.design.framework.link.multilink.chain.BusinessLinkedList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

/**
 * 结算规则链工厂
 */
@Service
public class GroupBuySettlementRuleFilterFactory {

    /**
     *
     * @param outTradeNoRuleFilter
     * @param settableRuleFilter
     * @param endRuleFilter
     * @return
     */
    @Bean("groupBuySettlementRuleFilter")
    public BusinessLinkedList<
            GroupBuySettlementCommandEntity,
            DynamicContext,
            GroupBuySettlementFeedBackEntity> groupBuySettlementRuleFilter
            (OutTradeNoRuleFilter outTradeNoRuleFilter,
             SettableRuleFilter settableRuleFilter,
             EndRuleFilter endRuleFilter) {
        return new LinkArmory<>(
                "拼团组队结算规则过滤链",
                outTradeNoRuleFilter,
                settableRuleFilter,
                endRuleFilter
        ).getLogicLink();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DynamicContext {

        private GroupBuyOrderEntity groupBuyOrderEntity;
        private GroupBuyTeamEntity groupBuyTeamEntity;

    }

}
