package cn.bugstack.trigger.http;

import cn.bugstack.api.IPayService;
import cn.bugstack.api.dto.CreatePayRequestDTO;
import cn.bugstack.api.response.Response;
import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.model.entity.ShopCartEntity;
import cn.bugstack.domain.order.service.IOrderService;
import cn.bugstack.types.enums.ResponseCode;
import com.alipay.api.internal.util.AlipaySignature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/alipay/")
public class AliPayController implements IPayService {

    @Value("${alipay.alipay_public_key}")
    private String alipayPublicKey;
    @Resource
    private IOrderService orderService;

    @RequestMapping(value = "create_pay_order", method = RequestMethod.POST)
    public Response<String> createPayOrder(@RequestBody CreatePayRequestDTO createPayRequestDTO) {
        log.info("创建支付单接口开始 request:{}", createPayRequestDTO);
        HttpServletRequest request = null;
        String openid = null;
        try {
            request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String userId = (String) request.getAttribute("openid");
            openid = userId;
            if (userId == null) {
                userId = createPayRequestDTO.getUserId();
            }
            String productId = createPayRequestDTO.getProductId();
            log.info("商品下单，根据商品ID创建支付单开始 userId:{} productId:{}", userId, productId);
            // 下单逻辑
            PayOrderEntity payOrderEntity = orderService.createOrder(ShopCartEntity.builder()
                    .userId(userId)
                    .productId(productId)
                    .build());
            log.info("商品下单，根据商品ID创建支付单完成 userId:{} productId:{} outTradeNo:{}", userId, productId, payOrderEntity.getOutTradeNo());
            return Response.<String>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(payOrderEntity.getPayUrl())
                    .build();
        } catch (Exception e) {
            log.error("商品下单，根据商品ID创建支付单失败 userId:{} productId:{}",
                    openid, createPayRequestDTO.getProductId(), e);
            return Response.<String>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "pay_notify", method = RequestMethod.POST)
    public String payNotify(HttpServletRequest request) {
        try {
            log.info("支付回调接口开始 tradeStatus:{}", request.getParameter("trade_status"));
            if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
                Map<String, String> params = new HashMap<>();
                Map<String, String[]>  requestParams = request.getParameterMap();
                for (String key : requestParams.keySet()) {
                    params.put(key, request.getParameter(key));
                }

                String outTradeNo = params.get("out_trade_no");
                String gmt_create = params.get("gmt_create");
                String trade_no = params.get("trade_no");

                String sign = request.getParameter("sign");
                String content = AlipaySignature.getSignCheckContentV1(params);
                boolean checkSignature = AlipaySignature.rsa256CheckContent(content, sign, alipayPublicKey, "UTF-8"); // 验证签名
                // 支付宝验签
                if (checkSignature) {
                    // 验签通过
                    log.info("支付回调，交易名称: {}", params.get("subject"));
                    log.info("支付回调，交易状态: {}", params.get("trade_status"));
                    log.info("支付回调，支付宝交易凭证号: {}", params.get("trade_no"));
                    log.info("支付回调，商户订单号: {}", params.get("out_trade_no"));
                    log.info("支付回调，交易金额: {}", params.get("total_amount"));
                    log.info("支付回调，买家在支付宝唯一id: {}", params.get("buyer_id"));
                    log.info("支付回调，买家付款时间: {}", params.get("gmt_payment"));
                    log.info("支付回调，买家付款金额: {}", params.get("buyer_pay_amount"));
                    log.info("支付回调，支付回调，更新订单 {}", outTradeNo);
                    // 更新订单已支付
                    orderService.changeOrderPaySuccess(outTradeNo, parseAlipayTime(params.get("gmt_payment")));
                }
            }
            log.info("支付回调接口完成 tradeStatus:{}", request.getParameter("trade_status"));
            return "success";
        } catch (Exception e) {
            log.error("支付回调，处理失败", e);
            return "false";
        }
    }

    private Date parseAlipayTime(String alipayTime) {
        if (alipayTime == null || alipayTime.trim().isEmpty()) {
            return null;
        }
        try {
            return Date.from(LocalDateTime.parse(alipayTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .atZone(ZoneId.systemDefault())
                    .toInstant());
        } catch (Exception e) {
            log.warn("支付回调，支付宝支付时间解析失败 alipayTime:{}", alipayTime, e);
            return null;
        }
    }
}
