package cn.bugstack.domain.order.model.aggregate;

import cn.bugstack.domain.order.model.entity.OrderLockEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.LocalDateTime;

/**
 * 锁单聚合体 —— 辅助构建锁单记录
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LockOrderAggregate {

    private OrderLockEntity orderLockEntity;

    /**
     * 构建锁单聚合体：生成 lockId，设置默认状态 LOCKED
     */
    public static LockOrderAggregate build(String productId) {
        OrderLockEntity lockEntity = OrderLockEntity.builder()
                .lockId(RandomStringUtils.randomNumeric(14))
                .productId(productId)
                .orderId("")
                .lockStatus("LOCKED")
                .lockTime(LocalDateTime.now())
                .build();

        return LockOrderAggregate.builder()
                .orderLockEntity(lockEntity)
                .build();
    }

}
