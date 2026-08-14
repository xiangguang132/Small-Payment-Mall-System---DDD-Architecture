package cn.bugstack.trigger.http;

import cn.bugstack.api.IPayService;
import cn.bugstack.api.dto.CreatePayRequestDTO;
import cn.bugstack.api.response.Response;
import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.model.entity.ShopCartEntity;
import cn.bugstack.domain.order.service.IOrderService;
import cn.bugstack.domain.payment.service.IAlipayNotifyTaskService;
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
    @Resource
    private IAlipayNotifyTaskService alipayNotifyTaskService;

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
        // 验签操作
        // 先创建一个map容器
        Map<String, String> params = new HashMap<>();
        // 使用map容器接住支付宝 发送过来的http传输的数据
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });

        // 验签操作开始
        String sign = params.remove("sign");
        params.remove("sign_type");
        String tradeStatus = params.get("trade_status");
        log.info("支付回调接口开始 tradeStatus:{}", tradeStatus);

        try {
            // 判空操作
            if (sign == null || sign.isEmpty()) {
                log.error("支付回调缺少 sign");
                return "false";
            }
            // 获取签证
            boolean checkSignature = AlipaySignature.rsa256CheckContent(
                    AlipaySignature.getSignCheckContentV1(params),
                    sign,
                    alipayPublicKey,
                    "UTF-8");
            if (!checkSignature) {
                log.error("支付回调验签失败 outTradeNo:{}", params.get("out_trade_no"));
                return "false";
            }

            if (!"TRADE_SUCCESS".equals(tradeStatus)) {
                return "success";
            }

            alipayNotifyTaskService.saveNotifyTask(params);
            log.info("支付回调写入任务完成 outTradeNo:{} tradeNo:{}",
                    params.get("out_trade_no"), params.get("trade_no"));
            return "success";
        } catch (Exception e) {
            log.error("支付回调处理失败 outTradeNo:{}", params.get("out_trade_no"), e);
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
