package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.groupbuy.model.entity.UserCouponEntity;
import cn.bugstack.domain.groupbuy.repository.IUserCouponRepository;
import cn.bugstack.infrastructure.dao.IUserCouponDao;
import cn.bugstack.infrastructure.dao.po.promotion.UserCoupon;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UserCouponRepository implements IUserCouponRepository {

    @Resource
    private IUserCouponDao userCouponDao;

    @Override
    public UserCouponEntity queryUserCouponByUserIdAndCouponId(String userId, String couponId) {
        UserCoupon userCoupon = userCouponDao.queryUserCouponByUserIdAndCouponId(userId, couponId);
        return toEntity(userCoupon);
    }

    @Override
    public List<UserCouponEntity> queryUserCouponListByUserId(String userId) {
        List<UserCoupon> list = userCouponDao.queryUserCouponListByUserId(userId);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public void saveUserCoupon(UserCouponEntity entity) {
        UserCoupon po = UserCoupon.builder()
                .couponUserId(entity.getCouponUserId())
                .userId(entity.getUserId())
                .couponId(entity.getCouponId())
                .sourceOrderNo(entity.getSourceOrderNo())
                .status(entity.getStatus())
                .expireTime(entity.getExpireTime())
                .usedTime(entity.getUsedTime())
                .build();
        userCouponDao.insertUserCoupon(po);
    }

    @Override
    public List<UserCouponEntity> queryUserCouponPage(String userId, Integer status, Integer offset, Integer limit) {
        List<UserCoupon> list = userCouponDao.queryUserCouponPage(userId, status, offset, limit);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public long countUserCouponPage(String userId, Integer status) {
        return userCouponDao.countUserCouponPage(userId, status);
    }

    @Override
    public int writeOffUserCoupons(String userId, List<String> couponIds, String sourceOrderNo, LocalDateTime usedTime) {
        if (couponIds == null || couponIds.isEmpty()) {
            return 0;
        }
        return userCouponDao.batchUpdateUserCouponUsed(userId, couponIds, sourceOrderNo, usedTime);
    }

    private UserCouponEntity toEntity(UserCoupon userCoupon) {
        if (userCoupon == null) {
            return null;
        }
        return UserCouponEntity.builder()
                .id(userCoupon.getId())
                .couponUserId(userCoupon.getCouponUserId())
                .userId(userCoupon.getUserId())
                .couponId(userCoupon.getCouponId())
                .sourceOrderNo(userCoupon.getSourceOrderNo())
                .status(userCoupon.getStatus())
                .expireTime(userCoupon.getExpireTime())
                .usedTime(userCoupon.getUsedTime())
                .build();
    }
}
