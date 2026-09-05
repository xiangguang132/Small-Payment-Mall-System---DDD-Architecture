package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.promotion.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ICouponDao {

    Coupon queryCouponByCouponId(@Param("couponId") String couponId);

    List<Coupon> queryCouponPage(@Param("status") Integer status,
                                 @Param("couponType") String couponType,
                                 @Param("offset") Integer offset,
                                 @Param("limit") Integer limit);

    long countCouponPage(@Param("status") Integer status,
                         @Param("couponType") String couponType);

}
