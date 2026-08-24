package cn.bugstack.domain.groupbuy.service.refund.factory;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyOrderEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderBehaviorEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderCommandEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTeamEntity;
import cn.bugstack.domain.groupbuy.service.refund.filter.DataNodeFilter;
import cn.bugstack.domain.groupbuy.service.refund.filter.RefundOrderNodeFilter;
import cn.bugstack.domain.groupbuy.service.refund.filter.UniqueRefundNodeFilter;
import cn.bugstack.types.design.framework.link.multilink.chain.BusinessLinkedList;
import cn.bugstack.types.design.framework.link.multilink.LinkArmory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 拼团退单-责任链工厂
 * 组装数据加载、重复检查、退单执行三个过滤器
 */
@Configuration
public class GroupBuyRefundOrderRuleFilterFactory {

    @Bean
    public BusinessLinkedList<GroupBuyRefundOrderCommandEntity, GroupBuyRefundOrderRuleFilterFactory.DynamicContext, GroupBuyRefundOrderBehaviorEntity> groupBuyOrderRefundFilter(
            DataNodeFilter dataNodeFilter,
            UniqueRefundNodeFilter uniqueRefundNodeFilter,
            RefundOrderNodeFilter refundOrderNodeFilter) {
        return new LinkArmory<>(
                "拼团退单规则过滤链",
                dataNodeFilter,
                uniqueRefundNodeFilter,
                refundOrderNodeFilter
        ).getLogicLink();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        /**
         * 订单信息
         */
        private GroupBuyOrderEntity groupBuyOrderEntity;

        /**
         * 拼团信息
         */
        private GroupBuyTeamEntity groupBuyTeamEntity;

        /**
         * 活动信息
         */
        private GroupBuyActivityEntity groupBuyActivityEntity;

        /**
         * 是否通过重复退单校验
         */
        private boolean uniquePass;

        /**
         * 退单提示信息
         */
        private String message;

    }
}
