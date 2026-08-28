package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.groupbuy.model.entity.CouponEntity;
import cn.bugstack.domain.groupbuy.repository.ICouponRepository;
import cn.bugstack.infrastructure.dao.ICouponDao;
import cn.bugstack.infrastructure.dao.po.promotion.Coupon;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CouponRepository implements ICouponRepository {

    @Resource
    private ICouponDao couponDao;

    @Override
    public CouponEntity queryCouponByCouponId(String couponId) {
        Coupon coupon = couponDao.queryCouponByCouponId(couponId);
        return toEntity(coupon);
    }

    @Override
    public List<CouponEntity> queryCouponPage(Integer status, Integer offset, Integer limit) {
        List<Coupon> list = couponDao.queryCouponPage(status, offset, limit);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public long countCouponPage(Integer status) {
        return couponDao.countCouponPage(status);
    }

    private CouponEntity toEntity(Coupon coupon) {
        if (coupon == null) {
            return null;
        }
        return CouponEntity.builder()
                .id(coupon.getId())
                .couponId(coupon.getCouponId())
                .couponName(coupon.getCouponName())
                .couponType(coupon.getCouponType())
                .thresholdAmount(coupon.getThresholdAmount())
                .discountAmount(coupon.getDiscountAmount())
                .discountRate(coupon.getDiscountRate())
                .status(coupon.getStatus())
                .startTime(coupon.getStartTime())
                .endTime(coupon.getEndTime())
                .build();
    }
}
