package cn.bugstack.domain.payment.service.rule.factory;

import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.payment.model.entity.AlipayNotifyProcessCommandEntity;
import cn.bugstack.domain.payment.model.entity.AlipayNotifyProcessFeedBackEntity;
import cn.bugstack.domain.payment.model.entity.AlipayNotifyTaskEntity;
import cn.bugstack.domain.payment.service.rule.filter.DirectOrderFilter;
import cn.bugstack.domain.payment.service.rule.filter.GroupBuyOrderFilter;
import cn.bugstack.domain.payment.service.rule.filter.TaskExistsFilter;
import cn.bugstack.domain.payment.service.rule.filter.TaskStatusFilter;
import cn.bugstack.types.design.framework.link.multilink.LinkArmory;
import cn.bugstack.types.design.framework.link.multilink.chain.BusinessLinkedList;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

/**
 * 支付宝通知处理-规则链工厂
 */
@Service
public class AlipayNotifyProcessRuleFilterFactory {

    @Bean("alipayNotifyProcessRuleFilter")
    public BusinessLinkedList<
            AlipayNotifyProcessCommandEntity,
            DynamicContext,
            AlipayNotifyProcessFeedBackEntity> alipayNotifyProcessRuleFilter(
            TaskExistsFilter taskExistsFilter,
            TaskStatusFilter taskStatusFilter,
            DirectOrderFilter directOrderFilter,
            GroupBuyOrderFilter groupBuyOrderFilter) {
        return new LinkArmory<>(
                "支付宝通知处理规则过滤链",
                taskExistsFilter,
                taskStatusFilter,
                directOrderFilter,
                groupBuyOrderFilter
        ).getLogicLink();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DynamicContext {

        private AlipayNotifyTaskEntity task;

        private PayOrderEntity payOrder;

        private JSONObject params;

    }

}
