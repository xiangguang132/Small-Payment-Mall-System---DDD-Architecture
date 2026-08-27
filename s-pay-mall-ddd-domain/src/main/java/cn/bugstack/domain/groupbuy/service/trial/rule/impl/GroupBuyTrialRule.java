package cn.bugstack.domain.groupbuy.service.trial.rule.impl;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyDiscountEntity;
import cn.bugstack.domain.groupbuy.model.entity.TrialRuleResult;
import cn.bugstack.domain.groupbuy.model.valobj.TrialRuleTypeEnum;
import cn.bugstack.domain.groupbuy.service.discount.IGroupBuyDiscountService;
import cn.bugstack.domain.groupbuy.service.trial.rule.ITrialRule;
import cn.bugstack.domain.groupbuy.service.trial.rule.TrialRuleContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
public class GroupBuyTrialRule implements ITrialRule {

    @Resource
    private Map<String, IGroupBuyDiscountService> discountServiceMap;

    @Override
    public TrialRuleTypeEnum getRuleType() {
        return TrialRuleTypeEnum.GROUP_BUY;
    }

    @Override
    public boolean match(TrialRuleContext context) {
        return context != null
                && context.getDiscount() != null
                && context.getCurrentPrice() != null;
    }

    @Override
    public TrialRuleResult calculate(TrialRuleContext context) {
        GroupBuyDiscountEntity discount = context.getDiscount();
        BigDecimal currentPrice = context.getCurrentPrice();
        if (currentPrice == null) {
            currentPrice = context.getOriginalPrice();
        }

        if (discount == null || currentPrice == null) {
            return TrialRuleResult.builder()
                    .ruleType(getRuleType())
                    .matched(false)
                    .stackable(true)
                    .message("拼团折扣信息缺失")
                    .build();
        }

        IGroupBuyDiscountService discountService = discountServiceMap.get(discount.getMarketPlan());
        if (discountService == null) {
            log.warn("未找到拼团折扣实现, marketPlan={}", discount.getMarketPlan());
            return TrialRuleResult.builder()
                    .ruleType(getRuleType())
                    .ruleCode(discount.getDiscountId())
                    .ruleName(discount.getDiscountName())
                    .originalPrice(currentPrice)
                    .currentPrice(currentPrice)
                    .payPrice(currentPrice)
                    .deductionPrice(BigDecimal.ZERO)
                    .matched(false)
                    .stackable(true)
                    .message("未找到拼团折扣实现")
                    .build();
        }

        BigDecimal payPrice = discountService.calculate(context.getUserId(), currentPrice, discount);
        if (payPrice == null) {
            payPrice = currentPrice;
        }

        BigDecimal deductionPrice = currentPrice.subtract(payPrice);
        if (deductionPrice.compareTo(BigDecimal.ZERO) < 0) {
            deductionPrice = BigDecimal.ZERO;
        }

        return TrialRuleResult.builder()
                .ruleType(getRuleType())
                .ruleCode(discount.getDiscountId())
                .ruleName(discount.getDiscountName())
                .originalPrice(context.getOriginalPrice() == null ? currentPrice : context.getOriginalPrice())
                .currentPrice(currentPrice)
                .deductionPrice(deductionPrice)
                .payPrice(payPrice)
                .matched(payPrice.compareTo(currentPrice) < 0)
                .stackable(true)
                .message(discount.getDiscountDesc())
                .build();
    }
}