package cn.bugstack.infrastructure.dao.po.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单明细持久化对象（对应 pay_order_item 表）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayOrderItem {

    /** 自增ID */
    private Long id;

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

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

}