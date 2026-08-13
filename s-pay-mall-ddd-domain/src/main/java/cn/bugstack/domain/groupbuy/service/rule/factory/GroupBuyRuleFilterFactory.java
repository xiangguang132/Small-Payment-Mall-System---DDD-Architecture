package cn.bugstack.domain.groupbuy.service.rule.factory;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRuleCommandEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRuleFilterFeedBackEntity;
import cn.bugstack.domain.groupbuy.service.rule.filter.ActivityUsabilityRuleFilter;
import cn.bugstack.domain.groupbuy.service.rule.filter.UserTakeLimitRuleFilter;
import cn.bugstack.types.design.framework.link.multilink.LinkArmory;
import cn.bugstack.types.design.framework.link.multilink.chain.BusinessLinkedList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

/**
 * 规则链工厂
 */
@Service
public class GroupBuyRuleFilterFactory {

    @Bean("groupBuyRuleFilter")
    public BusinessLinkedList<GroupBuyRuleCommandEntity, DynamicContext, GroupBuyRuleFilterFeedBackEntity> groupBuyRuleFilter(
            ActivityUsabilityRuleFilter activityUsabilityRuleFilter,
            UserTakeLimitRuleFilter userTakeLimitRuleFilter) {
        return new LinkArmory<>(
                "拼团活动交易规则过滤链",
                activityUsabilityRuleFilter,
                userTakeLimitRuleFilter
        ).getLogicLink();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DynamicContext {

        private GroupBuyActivityEntity activity;

    }
}
