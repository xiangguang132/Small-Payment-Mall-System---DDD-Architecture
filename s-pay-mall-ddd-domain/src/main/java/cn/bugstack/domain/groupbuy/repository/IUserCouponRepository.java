package cn.bugstack.domain.groupbuy.repository;

import cn.bugstack.domain.groupbuy.model.entity.UserCouponEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface IUserCouponRepository {

    UserCouponEntity queryUserCouponByUserIdAndCouponId(String userId, String couponId);

    List<UserCouponEntity> queryUserCouponListByUserId(String userId);

    void saveUserCoupon(UserCouponEntity entity);

    List<UserCouponEntity> queryUserCouponPage(String userId, Integer status, Integer offset, Integer limit);

    long countUserCouponPage(String userId, Integer status);

    /**
     * 核销优惠券：将指定用户优惠券标记为已使用，写入来源单号和使用时间
     *
     * @param userId         用户ID
     * @param couponIds      要核销的优惠券ID列表
     * @param sourceOrderNo  来源订单号
     * @param usedTime       使用时间
     * @return 影响行数
     */
    int writeOffUserCoupons(String userId, List<String> couponIds, String sourceOrderNo, LocalDateTime usedTime);

}