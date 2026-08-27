package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.promotion.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IUserCouponDao {

    UserCoupon queryUserCouponByUserIdAndCouponId(@Param("userId") String userId,
                                                  @Param("couponId") String couponId);

    List<UserCoupon> queryUserCouponListByUserId(@Param("userId") String userId);

}
