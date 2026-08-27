package cn.bugstack.infrastructure.dao.po.promotion;

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
public class Coupon {

    private Long id;
    private String couponId;
    private String couponName;
    private Integer couponType;
    private BigDecimal discountValue;
    private BigDecimal thresholdAmount;
    private Integer scopeType;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
