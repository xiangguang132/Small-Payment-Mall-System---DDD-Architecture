package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.promotion.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface IUserCouponDao {

    UserCoupon queryUserCouponByUserIdAndCouponId(@Param("userId") String userId,
                                                  @Param("couponId") String couponId);

    List<UserCoupon> queryUserCouponListByUserId(@Param("userId") String userId);

    void insertUserCoupon(UserCoupon userCoupon);

    List<UserCoupon> queryUserCouponPage(@Param("userId") String userId,
                                         @Param("status") Integer status,
                                         @Param("offset") Integer offset,
                                         @Param("limit") Integer limit);

    long countUserCouponPage(@Param("userId") String userId,
                             @Param("status") Integer status);

    /**
     * 批量核销用户优惠券：status 0→1，写入 sourceOrderNo 和 usedTime
     */
    int batchUpdateUserCouponUsed(@Param("userId") String userId,
                                  @Param("couponIds") List<String> couponIds,
                                  @Param("sourceOrderNo") String sourceOrderNo,
                                  @Param("usedTime") LocalDateTime usedTime);

}
