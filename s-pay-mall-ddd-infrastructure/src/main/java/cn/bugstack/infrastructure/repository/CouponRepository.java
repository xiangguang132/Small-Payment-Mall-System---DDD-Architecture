package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.groupbuy.model.entity.CouponEntity;
import cn.bugstack.domain.groupbuy.repository.ICouponRepository;
import cn.bugstack.infrastructure.adapter.repository.AbstractRepository;
import cn.bugstack.infrastructure.dao.ICouponDao;
import cn.bugstack.infrastructure.dao.po.promotion.Coupon;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CouponRepository extends AbstractRepository implements ICouponRepository {

    @Resource
    private ICouponDao couponDao;

    @Override
    public CouponEntity queryCouponByCouponId(String couponId) {
        return getFromCacheOrDb(
                cacheKeyByCouponId(couponId),
                () -> {
                    Coupon coupon = couponDao.queryCouponByCouponId(couponId);
                    return toEntity(coupon);
                },
                30 * 60 * 1000L
        );
    }

    @Override
    public List<CouponEntity> queryCouponPage(Integer status, String couponType, Integer offset, Integer limit) {
        return getFromCacheOrDb(
                cacheKeyCouponPage(status, couponType, offset, limit),
                () -> {
                    List<Coupon> list = couponDao.queryCouponPage(status, couponType, offset, limit);
                    if (list == null || list.isEmpty()) {
                        return Collections.emptyList();
                    }
                    return list.stream().map(this::toEntity).collect(Collectors.toList());
                },
                10 * 60 * 1000L
        );
    }

    @Override
    public long countCouponPage(Integer status, String couponType) {
        return couponDao.countCouponPage(status, couponType);
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

    private String cacheKeyByCouponId(String couponId) {
        return "s-pay-mall:coupon:id:" + couponId;
    }

    private String cacheKeyCouponPage(Integer status, String couponType, Integer offset, Integer limit) {
        return "s-pay-mall:coupon:page:" + status + ":" + couponType + ":" + offset + ":" + limit;
    }
}
