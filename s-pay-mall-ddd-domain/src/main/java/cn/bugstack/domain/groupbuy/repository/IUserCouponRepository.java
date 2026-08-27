package cn.bugstack.domain.groupbuy.repository;

import cn.bugstack.domain.groupbuy.model.entity.UserCouponEntity;

import java.util.List;

public interface IUserCouponRepository {

    UserCouponEntity queryUserCouponByUserIdAndCouponId(String userId, String couponId);

    List<UserCouponEntity> queryUserCouponListByUserId(String userId);

}