package cn.bugstack.domain.groupbuy.service.trial.rule.coupon.impl;

import cn.bugstack.domain.groupbuy.model.entity.CouponEntity;
import cn.bugstack.domain.groupbuy.service.trial.rule.coupon.AbstractCouponCalculateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 折扣券 - 乘以 discountRate（如 0.9000 表示9折）
 * 例：原价 100，discountRate=0.9 → 实付 90
 */
@Slf4j
@Service("DISCOUNT")
public class DiscountCouponCalculateService extends AbstractCouponCalculateService {

    @Override
    protected BigDecimal doCalculate(BigDecimal currentPrice, CouponEntity coupon) {
        log.info("折扣券计算: currentPrice={}, discountRate={}", currentPrice, coupon.getDiscountRate());
        if (coupon.getDiscountRate() == null) {
            return null;
        }
        return currentPrice.multiply(coupon.getDiscountRate());
    }

}
