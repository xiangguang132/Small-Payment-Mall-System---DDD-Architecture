package cn.bugstack.domain.groupbuy.service.trial.rule.coupon.impl;

import cn.bugstack.domain.groupbuy.model.entity.CouponEntity;
import cn.bugstack.domain.groupbuy.service.trial.rule.coupon.AbstractCouponCalculateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 满减券 - 直接减去 discountAmount（门槛已在 AbstractCouponCalculateService 校验）
 * 例：满 100 减 20，原价 150 → 实付 130
 */
@Slf4j
@Service("FULL_REDUCTION")
public class FullReductionCouponCalculateService extends AbstractCouponCalculateService {

    @Override
    protected BigDecimal doCalculate(BigDecimal currentPrice, CouponEntity coupon) {
        log.info("满减券计算: currentPrice={}, discountAmount={}", currentPrice, coupon.getDiscountAmount());
        if (coupon.getDiscountAmount() == null) {
            return null;
        }
        return currentPrice.subtract(coupon.getDiscountAmount());
    }

}
