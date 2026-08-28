package cn.bugstack.domain.groupbuy.service.trial.rule.coupon.impl;

import cn.bugstack.domain.groupbuy.model.entity.CouponEntity;
import cn.bugstack.domain.groupbuy.service.trial.rule.coupon.AbstractCouponCalculateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 固定金额券 - 直接减去 discountAmount
 * 例：原价 100，discountAmount=20 → 实付 80
 */
@Slf4j
@Service("FIXED")
public class FixedCouponCalculateService extends AbstractCouponCalculateService {

    @Override
    protected BigDecimal doCalculate(BigDecimal currentPrice, CouponEntity coupon) {
        log.info("固定金额券计算: currentPrice={}, discountAmount={}", currentPrice, coupon.getDiscountAmount());
        if (coupon.getDiscountAmount() == null) {
            return null;
        }
        return currentPrice.subtract(coupon.getDiscountAmount());
    }

}
