package cn.bugstack.domain.groupbuy.service.trial.rule.impl;

import cn.bugstack.domain.groupbuy.model.entity.PointsEntity;
import cn.bugstack.domain.groupbuy.model.entity.TrialRuleResult;
import cn.bugstack.domain.groupbuy.model.valobj.TrialRuleTypeEnum;
import cn.bugstack.domain.groupbuy.service.trial.rule.ITrialRule;
import cn.bugstack.domain.groupbuy.service.trial.rule.TrialRuleContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 规则链
 * 积分抵扣计算节点
 */
@Slf4j
@Service
public class PointsTrialRule implements ITrialRule {

    /**
     * 默认积分兑换比例：100 积分抵 1 元。
     * 后续如果业务有明确配置，建议从配置中心或积分账户配置中读取。
     */
    private static final BigDecimal POINTS_PER_YUAN = new BigDecimal("100");

    @Override
    public TrialRuleTypeEnum getRuleType() {
        return TrialRuleTypeEnum.POINTS;
    }

    @Override
    public boolean match(TrialRuleContext context) {
        return context != null
                && context.getCurrentPrice() != null
                && context.getPoints() != null
                && context.getPoints().getAvailablePoints() != null
                && context.getPoints().getAvailablePoints() > 0;
    }

    @Override
    public TrialRuleResult calculate(TrialRuleContext context) {
        PointsEntity points = context.getPoints();
        BigDecimal currentPrice = context.getCurrentPrice();
        if (points == null || currentPrice == null) {
            return TrialRuleResult.builder()
                    .ruleType(getRuleType())
                    .matched(false)
                    .stackable(true)
                    .message("积分账户信息缺失")
                    .build();
        }

        BigDecimal availableDeduction = new BigDecimal(points.getAvailablePoints())
                .divide(POINTS_PER_YUAN, 2, RoundingMode.DOWN);

        if (availableDeduction.compareTo(BigDecimal.ZERO) <= 0) {
            return TrialRuleResult.builder()
                    .ruleType(getRuleType())
                    .matched(false)
                    .stackable(true)
                    .message("可抵扣积分不足")
                    .originalPrice(context.getOriginalPrice())
                    .currentPrice(currentPrice)
                    .payPrice(currentPrice)
                    .deductionPrice(BigDecimal.ZERO)
                    .build();
        }

        BigDecimal deductionPrice = availableDeduction.min(currentPrice);
        BigDecimal payPrice = currentPrice.subtract(deductionPrice);
        if (payPrice.compareTo(BigDecimal.ZERO) <= 0) {
            payPrice = new BigDecimal("0.01");
            deductionPrice = currentPrice.subtract(payPrice);
        }

        log.info("积分抵扣计算完成, userId:{}, points:{}, deductionPrice:{}, payPrice:{}",
                context.getUserId(), points.getAvailablePoints(), deductionPrice, payPrice);

        return TrialRuleResult.builder()
                .ruleType(getRuleType())
                .ruleCode(String.valueOf(points.getId()))
                .ruleName("积分抵扣")
                .originalPrice(context.getOriginalPrice() == null ? currentPrice : context.getOriginalPrice())
                .currentPrice(currentPrice)
                .deductionPrice(deductionPrice)
                .payPrice(payPrice)
                .matched(deductionPrice.compareTo(BigDecimal.ZERO) > 0)
                .stackable(true)
                .message("积分抵扣成功")
                .build();
    }
}