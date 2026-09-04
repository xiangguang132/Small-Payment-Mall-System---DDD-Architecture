package cn.bugstack.domain.order.model.aggregate;

import cn.bugstack.domain.order.model.entity.OrderEntity;
import cn.bugstack.domain.order.model.entity.ProductEntity;
import cn.bugstack.domain.order.model.valobj.OrderStatusVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 聚合对象
 * 辅助实现数据入库的数据传输
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderAggregate {

    private String userId;

    private ProductEntity  productEntity;

    private OrderEntity orderEntity;

    /** 券后实付价（为 null 时取 productEntity.price） */
    private BigDecimal payAmount;

    /** 商品原价（券抵扣前，为 null 时取 productEntity.price） */
    private BigDecimal originalAmount;

    /** 使用的优惠券ID列表(JSON数组) */
    private String couponIds;

    public static OrderEntity buildOrderEntity(String productId, String productName) {
        return OrderEntity.builder()
                .productId(productId)
                .productName(productName)
                .outTradeNo(RandomStringUtils.randomNumeric(14))
                .orderTime(new Date())
                .orderStatus(OrderStatusVO.CREATE)
                .build();
    }
}
