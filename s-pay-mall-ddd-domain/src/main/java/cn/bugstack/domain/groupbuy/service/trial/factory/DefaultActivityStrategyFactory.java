package cn.bugstack.domain.groupbuy.service.trial.factory;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyDiscountEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialRequest;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialResult;
import cn.bugstack.domain.groupbuy.service.trial.node.RootNode;
import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import cn.bugstack.types.design.framework.tree.StrategyHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 一切决策的开始，从这里进入 RootNode ，然后开始走 规则树
 * 引入RootNode 作为后面转入的开口
 * 使用 StrategyHandler 决策默认进入RootNode节点
 * 定义动态上下文 DynamicContext
 */
@Service
public class DefaultActivityStrategyFactory {

    /**
     * 工厂的起始节点一定是根节点
     * 构造方法
     */
    private final RootNode rootNode;

    public DefaultActivityStrategyFactory(RootNode rootNode) {
        this.rootNode = rootNode;
    }

    /**
     * 默认策略执行器
     * 直接返回 RootNode
     * @return
     */
    public StrategyHandler<GroupBuyTrialRequest, DynamicContext, GroupBuyTrialResult> strategyHandler() {
        return rootNode;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        // 拼团活动配置对象
        private GroupBuyActivityEntity activity;
        // 拼团活动折扣对象
        private GroupBuyDiscountEntity discount;
        // 商品聚合体
        private ProductAggregate product;
        // 原价
        private BigDecimal originalPrice;
        // 支付价格
        private BigDecimal payPrice;
        // 折扣金额
        private BigDecimal deductionPrice;
        // 是否配置可见
        private boolean visible;
        // 是否配置可参与
        private boolean enable;
    }
}
