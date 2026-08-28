package cn.bugstack.domain.groupbuy.repository;

import cn.bugstack.domain.groupbuy.model.entity.CouponEntity;

import java.util.List;

public interface ICouponRepository {

    CouponEntity queryCouponByCouponId(String couponId);

    List<CouponEntity> queryCouponPage(Integer status, Integer offset, Integer limit);

    long countCouponPage(Integer status);

}