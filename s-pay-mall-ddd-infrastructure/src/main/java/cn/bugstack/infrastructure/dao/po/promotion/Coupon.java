package cn.bugstack.infrastructure.dao.po.promotion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券 PO - 对齐 group_buy_coupon 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    private Long id;
    private String couponId;
    private String couponName;
    /** 优惠券类型：FULL_REDUCTION / DISCOUNT / FIXED */
    private String couponType;
    private BigDecimal thresholdAmount;
    /** 优惠金额（FIXED/FULL_REDUCTION 使用） */
    private BigDecimal discountAmount;
    /** 折扣率，例如 0.9000（DISCOUNT 使用） */
    private BigDecimal discountRate;
    /** 状态：0停用 1启用 */
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
