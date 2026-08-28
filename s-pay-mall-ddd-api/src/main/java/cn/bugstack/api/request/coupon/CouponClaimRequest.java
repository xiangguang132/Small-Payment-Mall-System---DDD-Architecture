package cn.bugstack.api.request.coupon;

import lombok.Data;

/**
 * 领券中心-领券请求
 */
@Data
public class CouponClaimRequest {

    /** 优惠券ID */
    private String couponId;

}
