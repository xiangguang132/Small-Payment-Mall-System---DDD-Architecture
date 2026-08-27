package cn.bugstack.domain.groupbuy.repository;

import cn.bugstack.domain.groupbuy.model.entity.CouponEntity;

public interface ICouponRepository {

    CouponEntity queryCouponByCouponId(String couponId);

}