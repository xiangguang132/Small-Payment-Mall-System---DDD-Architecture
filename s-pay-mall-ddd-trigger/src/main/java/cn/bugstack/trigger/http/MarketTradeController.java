package cn.bugstack.trigger.http;

import cn.bugstack.api.request.trade.LockPayOrderRequest;
import cn.bugstack.api.request.trade.RefundOrderRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.trade.LockPayOrderResponse;
import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.model.entity.ShopCartEntity;
import cn.bugstack.domain.order.service.IOrderService;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/trade/")
public class MarketTradeController {

    @Resource
    private IOrderService orderService;

    /**
     * 普通下单（非拼团）锁单：复用 createOrder 的幂等锁单语义，
     * 返回结构化 LockPayOrderResponse 而非裸 payUrl 字符串。
     */
    @RequestMapping(value = "lock_pay_order", method = RequestMethod.POST)
    public Response<LockPayOrderResponse> lockPayOrder(@RequestBody LockPayOrderRequest request) {
        log.info("普通下单锁单开始 request:{}", request);
        String openid = null;
        try {
            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String userId = (String) httpRequest.getAttribute("openid");
            openid = userId;
            if (userId == null) {
                userId = request.getUserId();
            }
            String productId = request.getProductId();
            log.info("普通下单锁单，userId:{} productId:{}", userId, productId);

            PayOrderEntity payOrderEntity = orderService.createOrder(ShopCartEntity.builder()
                    .userId(userId)
                    .productId(productId)
                    .build());

            LockPayOrderResponse lockPayOrderResponse = LockPayOrderResponse.builder()
                    .outTradeNo(payOrderEntity.getOutTradeNo())
                    .payUrl(payOrderEntity.getPayUrl())
                    .build();

            log.info("普通下单锁单完成 userId:{} productId:{} outTradeNo:{}", userId, productId, payOrderEntity.getOutTradeNo());
            return Response.<LockPayOrderResponse>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(lockPayOrderResponse)
                    .build();
        } catch (Exception e) {
            log.error("普通下单锁单失败 userId:{} productId:{}", openid, request.getProductId(), e);
            return Response.<LockPayOrderResponse>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 退单（支持已支付退回到支付宝、未支付直接关单）。
     * 归属校验在 service 层完成，防止越权退他人的单。
     */
    @RequestMapping(value = "refund_order", method = RequestMethod.POST)
    public Response<String> refundOrder(@RequestBody RefundOrderRequest request) {
        log.info("退单接口开始 request:{}", request);
        String openid = null;
        try {
            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String userId = (String) httpRequest.getAttribute("openid");
            openid = userId;
            if (userId == null) {
                userId = request.getUserId();
            }
            String outTradeNo = request.getOutTradeNo();
            log.info("退单开始 userId:{} outTradeNo:{}", userId, outTradeNo);

            orderService.refundOrder(userId, outTradeNo);

            log.info("退单完成 userId:{} outTradeNo:{}", userId, outTradeNo);
            return Response.<String>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data("success")
                    .build();
        } catch (AppException e) {
            // 归属校验失败(403)、订单不存在(404)等业务异常交由全局异常处理器返回对应语义
            throw e;
        } catch (Exception e) {
            log.error("退单失败 userId:{} outTradeNo:{}", openid, request.getOutTradeNo(), e);
            return Response.<String>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

}
