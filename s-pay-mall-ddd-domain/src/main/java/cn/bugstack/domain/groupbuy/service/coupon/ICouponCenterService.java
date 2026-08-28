package cn.bugstack.domain.groupbuy.service.coupon;

import cn.bugstack.domain.groupbuy.model.entity.CouponEntity;

import java.util.List;

/**
 * 领券中心服务接口
 */
public interface ICouponCenterService {

    /**
     * 分页查询优惠券列表
     * @param status 状态筛选 null=全部启用
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return 优惠券列表
     */
    List<CouponEntity> queryCouponPage(Integer status, Integer pageNo, Integer pageSize);

    /**
     * 统计优惠券数量
     */
    long countCouponPage(Integer status);

    /**
     * 领取优惠券
     * @param userId 用户ID
     * @param couponId 优惠券ID
     */
    void claimCoupon(String userId, String couponId);

    /**
     * 查询用户可用优惠券（status=0 未使用）
     * @param userId 用户ID
     * @return 可用优惠券详情列表
     */
    List<CouponEntity> queryMyAvailableCoupons(String userId);

}
