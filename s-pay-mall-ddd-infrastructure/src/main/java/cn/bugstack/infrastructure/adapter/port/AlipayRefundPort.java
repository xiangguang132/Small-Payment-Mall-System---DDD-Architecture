package cn.bugstack.infrastructure.adapter.port;

import cn.bugstack.domain.payment.adapter.port.IAlipayPort;
import cn.bugstack.domain.payment.adapter.port.IAlipayRefundPort;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
@Component
public class AlipayRefundPort implements IAlipayRefundPort {

    @Resource
    private AlipayClient alipayClient;

    @Override
    public boolean refund(String outTradeNo, String outRequestNo, BigDecimal refundAmount) {
        AlipayTradeRefundRequest alipayTradeRefundRequest = new AlipayTradeRefundRequest();
        JSONObject bizContent = new JSONObject();
        // 存入 商户订单号、退款金额、部分退款的流水号（全退可省略）
        bizContent.put("out_trade_no", outTradeNo);   // 商户订单号
        bizContent.put("refund_amount", refundAmount); // 退款金额
        bizContent.put("out_request_no", outRequestNo); // 部分退款流水号，全退可省略
        alipayTradeRefundRequest.setBizContent(bizContent.toString());
        try {
            AlipayTradeRefundResponse alipayTradeRefundResponse = alipayClient.execute(alipayTradeRefundRequest);
            return alipayTradeRefundResponse.isSuccess();
        } catch (Exception e) {
            log.error("支付宝退款失败 outTradeNo:{}", outTradeNo, e);
            return false;
        }
    }
}
