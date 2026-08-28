package cn.bugstack.domain.groupbuy.service.trial.rule.impl;

import cn.bugstack.domain.groupbuy.model.entity.CouponEntity;
import cn.bugstack.domain.groupbuy.model.entity.TrialRuleResult;
import cn.bugstack.domain.groupbuy.model.valobj.TrialRuleTypeEnum;
import cn.bugstack.domain.groupbuy.service.trial.rule.coupon.ICouponCalculateService;
import cn.bugstack.domain.groupbuy.service.trial.rule.ITrialRule;
import cn.bugstack.domain.groupbuy.service.trial.rule.TrialRuleContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 规则链
 * 优惠券计算节点
 */
@Slf4j
@Service
public class CouponTrialRule implements ITrialRule {

    @Resource
    private Map<String, ICouponCalculateService> couponCalculateServiceMap;

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
            log.info("【价格流转-优惠券】未选择优惠券，跳过");
            return TrialRuleResult.builder()
                    .ruleType(getRuleType())
                    .matched(false)
                    .stackable(true)
                    .message("未选择优惠券")
                    .build();
        }

        log.info("【价格流转-优惠券】开始计算 当前价格:{} 候选券数:{}", currentPrice, couponList.size());

        CouponEntity bestCoupon = null;
        BigDecimal bestPayPrice = currentPrice;

        for (CouponEntity coupon : couponList) {
            if (coupon == null) {
                continue;
            }

            BigDecimal payPrice = calculatePayPrice(currentPrice, coupon);
            log.info("【价格流转-优惠券】券[{}] 类型:{} 试算结果:{}", coupon.getCouponName(), coupon.getCouponType(), payPrice);
            if (payPrice == null) {
                continue;
            }

            if (bestCoupon == null || payPrice.compareTo(bestPayPrice) < 0) {
                bestCoupon = coupon;
                bestPayPrice = payPrice;
            }
        }

        if (bestCoupon == null || bestPayPrice.compareTo(currentPrice) >= 0) {
            log.info("【价格流转-优惠券】无适用优惠券，保持当前价格:{}", currentPrice);
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

        log.info("【价格流转-优惠券】命中最优券[{}] 原价:{} 实付价:{} 抵扣:{}",
                bestCoupon.getCouponName(), currentPrice, bestPayPrice, deductionPrice);

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
        String couponType = coupon.getCouponType() != null ? coupon.getCouponType().trim() : null;
        if (couponType == null) {
            return null;
        }

        ICouponCalculateService calculateService = couponCalculateServiceMap.get(couponType);
        if (calculateService == null) {
            log.warn("未找到优惠券计算实现, couponType={}", couponType);
            return null;
        }

        return calculateService.calculate(currentPrice, coupon);
    }
}