package cn.bugstack.domain.groupbuy.service.trial.rule.impl;

import cn.bugstack.domain.groupbuy.model.entity.CouponEntity;
import cn.bugstack.domain.groupbuy.model.entity.TrialRuleResult;
import cn.bugstack.domain.groupbuy.model.valobj.TrialRuleTypeEnum;
import cn.bugstack.domain.groupbuy.service.trial.rule.ITrialRule;
import cn.bugstack.domain.groupbuy.service.trial.rule.TrialRuleContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 规则链
 * 优惠券计算节点
 */
@Slf4j
@Service
public class CouponTrialRule implements ITrialRule {

    @Override
    public TrialRuleTypeEnum getRuleType() {
        return TrialRuleTypeEnum.COUPON;
    }

    @Override
    public boolean match(TrialRuleContext context) {
        return context != null
                && context.getCurrentPrice() != null
                && context.getSelectedCoupons() != null
                && !context.getSelectedCoupons().isEmpty();
    }

    @Override
    public TrialRuleResult calculate(TrialRuleContext context) {
        List<CouponEntity> couponList = context.getSelectedCoupons();
        BigDecimal currentPrice = context.getCurrentPrice();
        if (couponList == null || couponList.isEmpty() || currentPrice == null) {
            return TrialRuleResult.builder()
                    .ruleType(getRuleType())
                    .matched(false)
                    .stackable(true)
                    .message("未选择优惠券")
                    .build();
        }

        CouponEntity bestCoupon = null;
        BigDecimal bestPayPrice = currentPrice;

        for (CouponEntity coupon : couponList) {
            if (coupon == null) {
                continue;
            }

            BigDecimal payPrice = calculatePayPrice(currentPrice, coupon);
            if (payPrice == null) {
                continue;
            }

            if (bestCoupon == null || payPrice.compareTo(bestPayPrice) < 0) {
                bestCoupon = coupon;
                bestPayPrice = payPrice;
            }
        }

        if (bestCoupon == null || bestPayPrice.compareTo(currentPrice) >= 0) {
            return TrialRuleResult.builder()
                    .ruleType(getRuleType())
                    .matched(false)
                    .stackable(true)
                    .message("优惠券未命中")
                    .originalPrice(context.getOriginalPrice())
                    .currentPrice(currentPrice)
                    .payPrice(currentPrice)
                    .deductionPrice(BigDecimal.ZERO)
                    .build();
        }

        BigDecimal deductionPrice = currentPrice.subtract(bestPayPrice);
        if (deductionPrice.compareTo(BigDecimal.ZERO) < 0) {
            deductionPrice = BigDecimal.ZERO;
        }

        return TrialRuleResult.builder()
                .ruleType(getRuleType())
                .ruleCode(bestCoupon.getCouponId())
                .ruleName(bestCoupon.getCouponName())
                .originalPrice(context.getOriginalPrice() == null ? currentPrice : context.getOriginalPrice())
                .currentPrice(currentPrice)
                .deductionPrice(deductionPrice)
                .payPrice(bestPayPrice)
                .matched(true)
                .stackable(true)
                .message("优惠券抵扣成功")
                .build();
    }

    private BigDecimal calculatePayPrice(BigDecimal currentPrice, CouponEntity coupon) {
        if (coupon.getStatus() != null && coupon.getStatus() != 1) {
            return null;
        }
        if (coupon.getStartTime() != null && coupon.getEndTime() != null) {
            // 时间范围校验后续如果要严格控制，可以由 MarketNode 提前过滤，这里先只做数据存在性校验
        }

        BigDecimal thresholdAmount = coupon.getThresholdAmount();
        if (thresholdAmount != null && currentPrice.compareTo(thresholdAmount) < 0) {
            return null;
        }

        BigDecimal discountValue = coupon.getDiscountValue();
        if (discountValue == null) {
            return null;
        }

        Integer couponType = coupon.getCouponType();
        if (couponType == null) {
            return null;
        }

        BigDecimal payPrice;
        switch (couponType) {
            case 0:
                // 直减
                payPrice = currentPrice.subtract(discountValue);
                break;
            case 1:
                // 折扣
                payPrice = currentPrice.multiply(discountValue);
                break;
            case 2:
                // 满减
                payPrice = currentPrice.subtract(discountValue);
                break;
            default:
                return null;
        }

        if (payPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return new BigDecimal("0.01");
        }
        return payPrice;
    }
}