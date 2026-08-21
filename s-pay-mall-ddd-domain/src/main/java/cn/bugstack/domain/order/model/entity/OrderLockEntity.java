package cn.bugstack.domain.order.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 锁单实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderLockEntity {

    private String lockId;
    private String userId;
    private String productId;
    private String productName;
    private BigDecimal totalAmount;
    private String lockStatus;  // LOCKED / CONFIRMED / EXPIRED
    private LocalDateTime lockTime;
    private LocalDateTime expireTime;

    /**
     * 判断是否过期
     * @return
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expireTime);
    }
}
