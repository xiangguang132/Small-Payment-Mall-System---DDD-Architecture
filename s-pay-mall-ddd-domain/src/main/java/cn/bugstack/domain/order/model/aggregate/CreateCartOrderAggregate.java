package cn.bugstack.domain.order.model.aggregate;

import cn.bugstack.domain.order.model.entity.OrderEntity;
import cn.bugstack.domain.order.model.entity.PayOrderItemEntity;
import cn.bugstack.domain.order.model.valobj.OrderStatusVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 购物车结算聚合：多商品聚合成一笔 pay_order + N 条 pay_order_item
 * 与 CreateOrderAggregate 的区别：头表用 items 明细，不落 productId/productName
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCartOrderAggregate {

    private String userId;

    private OrderEntity orderEntity;

    /** 结算明细（多商品） */
    private List<PayOrderItemEntity> items;

    /** 券后实付价 */
    private BigDecimal payAmount;

    /** 聚合原价（券抵扣前） */
    private BigDecimal originalAmount;

    /** 使用的优惠券ID列表(JSON数组) */
    private String couponIds;

    public static OrderEntity buildOrderEntity() {
        return OrderEntity.builder()
                .outTradeNo(RandomStringUtils.randomNumeric(14))
                .orderTime(new Date())
                .orderStatus(OrderStatusVO.CREATE)
                .build();
    }
}