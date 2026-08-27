package cn.bugstack.api.request.order;

import cn.bugstack.api.request.page.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PayOrderPageRequest extends PageRequest {

    /** 订单状态（null=全部）；CREATE-创建完成、PAY_WAIT-等待支付、PAY_SUCCESS-支付成功、DEAL_DONE-交易完成、CLOSE-订单关单、REFUNDING-退款中、REFUND-已退款 */
    private String status;

}
