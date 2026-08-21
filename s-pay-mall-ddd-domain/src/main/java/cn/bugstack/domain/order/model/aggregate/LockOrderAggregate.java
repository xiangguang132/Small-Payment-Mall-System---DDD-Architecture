package cn.bugstack.domain.order.model.aggregate;

import cn.bugstack.domain.order.model.entity.OrderLockEntity;
import cn.bugstack.domain.order.model.entity.ProductEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.LocalDateTime;

/**
 * 锁单聚合体 —— 组装锁单 + 商品快照
 * 参考 CreateOrderAggregate 的设计思路：
 *   CreateOrderAggregate = OrderEntity + ProductEntity → 辅助创建订单
 *   LockOrderAggregate   = OrderLockEntity + ProductEntity → 辅助锁单
 * 后续扩展点：
 *   - 库存锁定信息
 *   - 优惠券信息
 *   - 限购规则校验结果
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LockOrderAggregate {

    private String userId;
    private ProductEntity productEntity;
    private OrderLockEntity orderLockEntity;

    /**
     * 构建锁单聚合体
     * 接收 userId + 商品信息，内部生成 lockId 和锁单记录
     */
    public static LockOrderAggregate build(String userId, ProductEntity productEntity) {
        OrderLockEntity lockEntity = OrderLockEntity.builder()
                .lockId(RandomStringUtils.randomNumeric(14))
                .userId(userId)
                .productId(productEntity.getProductId())
                .productName(productEntity.getProductName())
                .totalAmount(productEntity.getPrice())
                .lockStatus("LOCKED")
                .lockTime(LocalDateTime.now())
                .expireTime(LocalDateTime.now().plusMinutes(15))
                .build();

        return LockOrderAggregate.builder()
                .userId(userId)
                .productEntity(productEntity)
                .orderLockEntity(lockEntity)
                .build();
    }

    /**
     * 从已有锁单记录 + 商品信息构建聚合体（幂等复用场景）
     */
    public static LockOrderAggregate fromExisting(OrderLockEntity lockEntity, ProductEntity productEntity) {
        return LockOrderAggregate.builder()
                .userId(lockEntity.getUserId())
                .productEntity(productEntity)
                .orderLockEntity(lockEntity)
                .build();
    }

}
