package cn.bugstack.domain.order.model.entity;

import cn.bugstack.domain.order.model.valobj.OrderStatusVO;
import cn.bugstack.types.enums.OrderTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayOrderEntity {

    private String userId;
    private String outTradeNo;
    private String payUrl;
    private OrderStatusVO orderStatus;
    private OrderTypeEnum orderType;
}
