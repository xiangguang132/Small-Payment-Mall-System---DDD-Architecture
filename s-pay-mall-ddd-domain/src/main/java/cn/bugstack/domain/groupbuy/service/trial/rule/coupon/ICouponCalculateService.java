package cn.bugstack.domain.groupbuy.service.trial.rule.coupon;

import cn.bugstack.domain.groupbuy.model.entity.CouponEntity;

import java.math.BigDecimal;

/**
 * 优惠券计算策略接口
 * 不同优惠券类型（FIXED / DISCOUNT / FULL_REDUCTION）各自实现具体的抵扣逻辑
 * 通过 Spring Bean Name 实现 Map 分发，key = couponType
 */
public interface ICouponCalculateService {

    /**
     * 计算使用优惠券后的实付价格
     *
     * @param currentPrice 当前价格（经过前置规则链计算后的价格）
     * @param coupon       优惠券实体
     * @return 实付价格；返回 null 表示该券不适用
     */
    BigDecimal calculate(BigDecimal currentPrice, CouponEntity coupon);

}
