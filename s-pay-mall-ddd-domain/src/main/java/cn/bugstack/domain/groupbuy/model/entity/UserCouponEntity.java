package cn.bugstack.domain.groupbuy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCouponEntity {

    private Long id;

    private String userId;

    private String couponId;

    /** 未使用、已使用、已过期 */
    private Integer status;

    private String orderId;

    private LocalDateTime useTime;

    private LocalDateTime expireTime;

}
