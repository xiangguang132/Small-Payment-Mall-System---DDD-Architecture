package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.groupbuy.model.entity.CouponEntity;
import cn.bugstack.domain.groupbuy.repository.ICouponRepository;
import cn.bugstack.infrastructure.dao.ICouponDao;
import cn.bugstack.infrastructure.dao.po.promotion.Coupon;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class CouponRepository implements ICouponRepository {

    @Resource
    private ICouponDao couponDao;

    public CouponEntity queryCouponByCouponId(String couponId) {
        Coupon coupon = couponDao.queryCouponByCouponId(couponId);
        if (coupon == null) {
            return null;
        }
        return CouponEntity.builder()
                .id(coupon.getId())
                .couponId(coupon.getCouponId())
                .couponName(coupon.getCouponName())
                .couponType(coupon.getCouponType())
                .discountValue(coupon.getDiscountValue())
                .thresholdAmount(coupon.getThresholdAmount())
                .scopeType(coupon.getScopeType())
                .status(coupon.getStatus())
                .startTime(coupon.getStartTime())
                .endTime(coupon.getEndTime())
                .build();
    }
}
