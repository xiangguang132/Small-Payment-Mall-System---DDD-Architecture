package cn.bugstack.domain.order.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单明细项（对应 pay_order_item 一行）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class   PayOrderItemEntity {

    /** 商户订单号（pay_order.out_trade_no） */
    private String orderId;

    /** 商品ID */
    private Long productId;

    /** 商品名称（快照） */
    private String productName;

    /** 数量 */
    private Integer quantity;

    /** 商品单价 */
    private BigDecimal price;

    /** 小计金额（price × quantity） */
    private BigDecimal totalAmount;

}