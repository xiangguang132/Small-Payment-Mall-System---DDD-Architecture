package cn.bugstack.infrastructure.dao.po.promotion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCoupon {

    private Long id;
    private String userId;
    private String couponId;
    private Integer status;
    private String orderId;
    private LocalDateTime useTime;
    private LocalDateTime expireTime;

}
