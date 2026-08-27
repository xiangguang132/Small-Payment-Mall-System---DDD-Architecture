package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.groupbuy.model.entity.UserCouponEntity;
import cn.bugstack.domain.groupbuy.repository.IUserCouponRepository;
import cn.bugstack.infrastructure.dao.IUserCouponDao;
import cn.bugstack.infrastructure.dao.po.promotion.UserCoupon;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UserCouponRepository implements IUserCouponRepository {

    @Resource
    private IUserCouponDao userCouponDao;

    public UserCouponEntity queryUserCouponByUserIdAndCouponId(String userId, String couponId) {
        UserCoupon userCoupon = userCouponDao.queryUserCouponByUserIdAndCouponId(userId, couponId);
        if (userCoupon == null) {
            return null;
        }
        return toEntity(userCoupon);
    }

    public List<UserCouponEntity> queryUserCouponListByUserId(String userId) {
        List<UserCoupon> userCouponList = userCouponDao.queryUserCouponListByUserId(userId);
        if (userCouponList == null || userCouponList.isEmpty()) {
            return Collections.emptyList();
        }
        return userCouponList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    private UserCouponEntity toEntity(UserCoupon userCoupon) {
        return UserCouponEntity.builder()
                .id(userCoupon.getId())
                .userId(userCoupon.getUserId())
                .couponId(userCoupon.getCouponId())
                .status(userCoupon.getStatus())
                .orderId(userCoupon.getOrderId())
                .useTime(userCoupon.getUseTime())
                .expireTime(userCoupon.getExpireTime())
                .build();
    }
}
