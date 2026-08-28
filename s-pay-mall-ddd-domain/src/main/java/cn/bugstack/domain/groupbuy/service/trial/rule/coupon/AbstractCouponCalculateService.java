package cn.bugstack.domain.groupbuy.service.trial.rule.coupon;

import cn.bugstack.domain.groupbuy.model.entity.CouponEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 优惠券计算策略 - 抽象基类（模板方法）
 * <p>
 * 提取三种券类型的公共逻辑：
 * 1. 状态校验（仅 status=1 启用的券可用）
 * 2. 门槛校验（当前价格需达到 thresholdAmount）
 * 3. 底价保护（实付价不低于 0.01）
 */
@Slf4j
@Service
public abstract class AbstractCouponCalculateService implements ICouponCalculateService {

    @Override
    public BigDecimal calculate(BigDecimal currentPrice, CouponEntity coupon) {
        // 1. 状态校验：仅 status=1（启用）的券可用
        if (coupon.getStatus() != null && coupon.getStatus() != 1) {
            log.info("【价格流转-券计算】券[{}] 状态不可用 status={}", coupon.getCouponName(), coupon.getStatus());
            return null;
        }

        // 2. 门槛校验：当前价格需达到满减门槛
        BigDecimal thresholdAmount = coupon.getThresholdAmount();
        if (thresholdAmount != null && currentPrice.compareTo(thresholdAmount) < 0) {
            log.info("【价格流转-券计算】券[{}] 未达门槛 当前价:{} 门槛:{}", coupon.getCouponName(), currentPrice, thresholdAmount);
            return null;
        }

        // 3. 委托子类实现具体计算
        BigDecimal payPrice = doCalculate(currentPrice, coupon);
        if (payPrice == null) {
            return null;
        }

        log.info("【价格流转-券计算】券[{}] 类型:{} 当前价:{} 计算后实付:{}", coupon.getCouponName(), coupon.getCouponType(), currentPrice, payPrice);

        // 4. 底价保护：实付价不低于 0.01
        if (payPrice.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("【价格流转-券计算】券[{}] 底价保护生效 0.01", coupon.getCouponName());
            return new BigDecimal("0.01");
        }
        return payPrice;
    }

    /**
     * 子类实现具体的优惠券价格计算逻辑
     *
     * @param currentPrice 当前价格
     * @param coupon       优惠券实体
     * @return 实付价格；返回 null 表示该券不适用
     */
    protected abstract BigDecimal doCalculate(BigDecimal currentPrice, CouponEntity coupon);

}
