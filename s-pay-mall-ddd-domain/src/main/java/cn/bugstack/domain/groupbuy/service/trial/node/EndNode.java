package cn.bugstack.domain.groupbuy.service.trial.node;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyDiscountEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialRequest;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialResult;
import cn.bugstack.domain.groupbuy.model.entity.TrialRuleResult;
import cn.bugstack.domain.groupbuy.service.trial.AbstractGroupBuyMarketSupport;
import cn.bugstack.domain.groupbuy.service.trial.factory.DefaultActivityStrategyFactory;
import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import cn.bugstack.types.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 结束节点
 * 组装 试算实体
 * 路由到默认策略执行器->结束
 * 使用TialBalanceEntity接取最终试算结果
 */
@Slf4j
@Service
public class EndNode extends AbstractGroupBuyMarketSupport {

    @Override
    protected GroupBuyTrialResult doApply(GroupBuyTrialRequest requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("拼团商品查询试算服务-EndNode userId:{} requestParameter:{}", requestParameter.getUserId(), JSON.toJSONString(requestParameter));

        GroupBuyActivityEntity activity = dynamicContext.getActivity();
        GroupBuyDiscountEntity discount = dynamicContext.getDiscount();
        ProductAggregate productAggregate = dynamicContext.getProduct();
        if (activity == null || productAggregate == null) {
            log.error("EndNode 上下文数据为空, userId:{}, activity:{}, product:{}, discount:{}",
                    requestParameter.getUserId(),
                    activity != null,
                    productAggregate != null,
                    discount != null);
            return GroupBuyTrialResult.builder()
                    .visible(false)
                    .enable(false)
                    .ruleDetails(dynamicContext.getAppliedRuleResults())
                    .build();
        }

        BigDecimal originalPrice = productAggregate.getPrice() == null
                ? BigDecimal.ZERO
                : productAggregate.getPrice();
        BigDecimal payPrice = dynamicContext.getPayPrice();
        BigDecimal deductionPrice = dynamicContext.getDeductionPrice();

        if (payPrice == null) {
            payPrice = deductionPrice == null
                    ? originalPrice
                    : originalPrice.subtract(deductionPrice);
        }
        if (deductionPrice == null) {
            deductionPrice = originalPrice.subtract(payPrice);
        }
        if (payPrice.compareTo(BigDecimal.ZERO) < 0) {
            payPrice = BigDecimal.ZERO;
        }
        if (deductionPrice.compareTo(BigDecimal.ZERO) < 0) {
            deductionPrice = BigDecimal.ZERO;
        }

        return GroupBuyTrialResult.builder()
                .activityId(activity.getActivityId())
                .activityName(activity.getActivityName())
                .groupType(activity.getGroupType())
                .takeLimitCount(activity.getTakeLimitCount())
                .targetCount(activity.getTargetCount())
                .validTime(activity.getValidTime())
                .status(activity.getStatus())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .tagId(activity.getTagId())
                .tagScope(activity.getTagScope())
                .productId(productAggregate.getId())
                .productName(productAggregate.getName())
                .originalPrice(originalPrice)
                .discountId(discount != null ? discount.getDiscountId() : null)
                .discountName(discount != null ? discount.getDiscountName() : null)
                .discountDesc(discount != null ? discount.getDiscountDesc() : null)
                .discountType(discount != null ? discount.getDiscountType() : null)
                .marketPlan(discount != null ? discount.getMarketPlan() : null)
                .marketExpr(discount != null ? discount.getMarketExpr() : null)
                .deductionPrice(deductionPrice)
                .payPrice(payPrice)
                .visible(dynamicContext.isVisible())
                .enable(dynamicContext.isEnable())
                .ruleDetails(dynamicContext.getAppliedRuleResults())
                .build();
    }

    @Override
    public StrategyHandler<GroupBuyTrialRequest, DefaultActivityStrategyFactory.DynamicContext, GroupBuyTrialResult> get(GroupBuyTrialRequest requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }
}