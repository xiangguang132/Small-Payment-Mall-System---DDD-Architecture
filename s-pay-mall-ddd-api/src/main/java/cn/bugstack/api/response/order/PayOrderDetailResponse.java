package cn.bugstack.api.response.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOrderDetailResponse {

    private Long id;

    private String userId;

    private String productId;

    private String productName;

    /** 商户订单号/支付宝 out_trade_no */
    private String outTradeNo;

    /** 下单时间 */
    private LocalDateTime orderTime;

    /** 订单金额 */
    private BigDecimal totalAmount;

    /** 订单类型；DIRECT普通购买、GROUP_BUY拼团 */
    private String orderType;

    /** 订单状态；CREATE-创建完成、PAY_WAIT-等待支付、PAY_SUCCESS-支付成功、DEAL_DONE-交易完成、CLOSE-订单关单、REFUNDING-退款中、REFUND-已退款 */
    private String status;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 外部交易时间 */
    private LocalDateTime outTradeTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
