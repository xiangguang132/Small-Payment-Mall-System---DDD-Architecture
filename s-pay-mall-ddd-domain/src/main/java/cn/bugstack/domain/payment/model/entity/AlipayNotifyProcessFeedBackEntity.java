package cn.bugstack.domain.payment.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付宝支付响应实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlipayNotifyProcessFeedBackEntity {

    private String outTradeNo;

    private boolean handled;

}
