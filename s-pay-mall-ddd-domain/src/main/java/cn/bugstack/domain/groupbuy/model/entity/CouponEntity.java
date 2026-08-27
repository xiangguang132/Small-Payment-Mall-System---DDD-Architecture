package cn.bugstack.domain.groupbuy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponEntity {

    private Long id;

    private String couponId;

    private String couponName;

    /** 优惠券类型：直减、折扣、满减 */
    private Integer couponType;

    private BigDecimal discountValue;

    private BigDecimal thresholdAmount;

    /** 适用范围：全场、指定商品、指定分类 */
    private Integer scopeType;

    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

}
