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
     * @param couponType 优惠券类型筛选 FULL_REDUCTION / DISCOUNT / FIXED，null=全部
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return 优惠券列表
     */
    List<CouponEntity> queryCouponPage(Integer status, String couponType, Integer pageNo, Integer pageSize);

    /**
     * 统计优惠券数量
     */
    long countCouponPage(Integer status, String couponType);

    /**
     * 领取优惠券
     * @param userId 用户ID
     * @param couponId 优惠券ID
     */
    void claimCoupon(String userId, String couponId);

    /**
     * 分页查询用户可用优惠券（status=0 未使用、启用且未过期）
     * @param userId 用户ID
     * @param couponType 优惠券类型筛选 FULL_REDUCTION / DISCOUNT / FIXED，null=全部
     * @param pageNo 页码（从 1 开始）
     * @param pageSize 每页大小
     * @return 当前页可用优惠券列表
     */
    List<CouponEntity> queryMyAvailableCoupons(String userId, String couponType, int pageNo, int pageSize);

    /**
     * 统计用户可用优惠券总数（与分页查询同一套过滤规则）
     * @param userId 用户ID
     * @param couponType 优惠券类型筛选 FULL_REDUCTION / DISCOUNT / FIXED，null=全部
     * @return 可用优惠券总数
     */
    long countMyAvailableCoupons(String userId, String couponType);

}
