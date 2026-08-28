package cn.bugstack.api.response.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 领券中心-优惠券详情响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponDetailResponse {

    private String couponId;
    private String couponName;
    /** 优惠券类型：FULL_REDUCTION / DISCOUNT / FIXED */
    private String couponType;
    private BigDecimal thresholdAmount;
    /** 优惠金额 */
    private BigDecimal discountAmount;
    /** 折扣率 */
    private BigDecimal discountRate;
    /** 状态：0停用 1启用 */
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
