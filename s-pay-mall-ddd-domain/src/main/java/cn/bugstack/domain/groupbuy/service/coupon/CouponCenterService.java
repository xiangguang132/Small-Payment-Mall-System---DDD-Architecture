package cn.bugstack.domain.groupbuy.service.coupon;

import cn.bugstack.domain.groupbuy.model.entity.CouponEntity;
import cn.bugstack.domain.groupbuy.model.entity.UserCouponEntity;
import cn.bugstack.domain.groupbuy.repository.ICouponRepository;
import cn.bugstack.domain.groupbuy.repository.IUserCouponRepository;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 领券中心服务实现
 */
@Slf4j
@Service
public class CouponCenterService implements ICouponCenterService {

    @Resource
    private ICouponRepository couponRepository;

    @Resource
    private IUserCouponRepository userCouponRepository;

    @Override
    public List<CouponEntity> queryCouponPage(Integer status, Integer pageNo, Integer pageSize) {
        int safePageNo = (pageNo == null || pageNo <= 0) ? 1 : pageNo;
        int safePageSize = (pageSize == null || pageSize <= 0) ? 10 : Math.min(pageSize, 100);
        int offset = (safePageNo - 1) * safePageSize;
        return couponRepository.queryCouponPage(status, offset, safePageSize);
    }

    @Override
    public long countCouponPage(Integer status) {
        return couponRepository.countCouponPage(status);
    }

    @Override
    public void claimCoupon(String userId, String couponId) {
        if (StringUtils.isBlank(userId)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "用户ID不能为空");
        }
        if (StringUtils.isBlank(couponId)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "优惠券ID不能为空");
        }

        // 1. 查询优惠券是否存在
        CouponEntity coupon = couponRepository.queryCouponByCouponId(couponId);
        if (coupon == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "优惠券不存在");
        }

        // 2. 校验优惠券是否启用
        if (coupon.getStatus() == null || coupon.getStatus() != 1) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "优惠券已停用");
        }

        // 3. 校验是否在有效期内
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartTime() != null && now.isBefore(coupon.getStartTime())) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "优惠券尚未生效");
        }
        if (coupon.getEndTime() != null && now.isAfter(coupon.getEndTime())) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "优惠券已过期");
        }

        // 4. 校验是否已领取过
        UserCouponEntity existing = userCouponRepository.queryUserCouponByUserIdAndCouponId(userId, couponId);
        if (existing != null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "您已领取过该优惠券");
        }

        // 5. 领取：插入 user_coupon 记录
        UserCouponEntity userCoupon = UserCouponEntity.builder()
                .couponUserId(UUID.randomUUID().toString().replace("-", ""))
                .userId(userId)
                .couponId(couponId)
                .status(0)  // 未使用
                .expireTime(coupon.getEndTime())
                .build();
        userCouponRepository.saveUserCoupon(userCoupon);

        log.info("用户领取优惠券成功 userId:{} couponId:{}", userId, couponId);
    }

    @Override
    public List<CouponEntity> queryMyAvailableCoupons(String userId) {
        // 查询用户未使用的券（status=0），不做分页限制（领券数量有限）
        List<UserCouponEntity> userCoupons = userCouponRepository.queryUserCouponPage(userId, 0, 0, 100);
        if (userCoupons == null || userCoupons.isEmpty()) {
            return Collections.emptyList();
        }
        // 批量加载券详情，过滤掉已过期的券
        LocalDateTime now = LocalDateTime.now();
        return userCoupons.stream()
                .map(uc -> couponRepository.queryCouponByCouponId(uc.getCouponId()))
                .filter(c -> c != null && c.getStatus() != null && c.getStatus() == 1)
                .filter(c -> c.getEndTime() == null || now.isBefore(c.getEndTime()))
                .collect(java.util.stream.Collectors.toList());
    }

}
