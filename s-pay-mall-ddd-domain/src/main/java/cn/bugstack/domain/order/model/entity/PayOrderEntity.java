package cn.bugstack.domain.order.model.entity;

import cn.bugstack.domain.order.model.valobj.OrderStatusVO;
import cn.bugstack.types.enums.OrderTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayOrderEntity {

    private Long id;
    private String userId;
    private String productId;
    private String productName;
    private String outTradeNo;
    private LocalDateTime orderTime;
    private BigDecimal totalAmount;
    /** 商品原价（券抵扣前） */
    private BigDecimal originalAmount;
    private OrderTypeEnum orderType;
    /** 使用的优惠券ID列表(JSON数组) */
    private String couponIds;
    private OrderStatusVO orderStatus;
    private String payUrl;
    private LocalDateTime payTime;
    private LocalDateTime outTradeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
