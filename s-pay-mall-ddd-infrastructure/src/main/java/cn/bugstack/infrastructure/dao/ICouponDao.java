package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.promotion.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ICouponDao {

    Coupon queryCouponByCouponId(@Param("couponId") String couponId);

}
