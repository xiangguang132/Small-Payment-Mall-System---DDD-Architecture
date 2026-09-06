package cn.bugstack.trigger.http;

import cn.bugstack.api.IPayService;
import cn.bugstack.api.request.trade.ConfirmOrderRequest;
import cn.bugstack.api.request.trade.DirectOrderTrialRequest;
import cn.bugstack.api.request.trade.LockOrderRequest;
import cn.bugstack.api.request.trade.RefundOrderRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.trade.ConfirmOrderResponse;
import cn.bugstack.api.response.trade.DirectOrderTrialResponse;
import cn.bugstack.api.response.trade.LockOrderResponse;
import cn.bugstack.domain.order.model.entity.OrderLockEntity;
import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.service.IOrderLockService;
import cn.bugstack.domain.order.service.IOrderService;
import cn.bugstack.domain.payment.service.IAlipayNotifyTaskService;
import cn.bugstack.domain.product.service.IProductService;
import cn.bugstack.infrastructure.event.EventPublisher;
import cn.bugstack.trigger.interceptor.PublicEndpoint;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import com.alipay.api.internal.util.AlipaySignature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
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
    @Value("${spring.rabbitmq.config.producer.topic_alipay_notify.routing_key}")
    private String alipayNotifyRoutingKey;
    @Resource
    private IOrderService orderService;
    @Resource
    private IOrderLockService orderLockService;
    @Resource
    private IAlipayNotifyTaskService alipayNotifyTaskService;
    @Resource
    private EventPublisher eventPublisher;
    @Resource
    private IProductService productService;

    /**
     * 锁单接口：构建聚合体 → 创建锁记录 → 返回 lockId
     * 锁单仅生成锁单号与状态；商品与用户信息在确认下单时由请求提供
     */
    @RequestMapping(value = "lock_order", method = RequestMethod.POST)
    public Response<LockOrderResponse> lockOrder(@RequestBody LockOrderRequest request) {
        log.info("锁单开始 request:{}", request);
        String userId = null;
        try {
            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            userId = (String) httpRequest.getAttribute("userId");

            OrderLockEntity lockEntity = orderLockService.lockOrder(request.getCouponIds());

            return Response.<LockOrderResponse>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(LockOrderResponse.builder()
                            .lockId(lockEntity.getLockId())
                            .lockTime(lockEntity.getLockTime())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("锁单失败 userId:{} productId:{}", userId, request.getProductId(), e);
            return Response.<LockOrderResponse>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 确认下单：携带 lockId + productId → 创建订单 → 返回 payUrl
     * 订单归属用户取自登录态 userId（兼容请求体 userId 兜底）
     */
    @PublicEndpoint
    @RequestMapping(value = "trial_direct_order", method = RequestMethod.POST)
    public Response<DirectOrderTrialResponse> trialDirectOrder(@RequestBody DirectOrderTrialRequest request) {
        log.info("直购试算开始 productId:{} couponIds:{}", request.getProductId(), request.getCouponIds());
        try {
            if (request.getProductId() == null) {
                return Response.<DirectOrderTrialResponse>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info("商品ID不能为空")
                        .build();
            }

            // 查询商品原价
            cn.bugstack.domain.product.model.aggregate.ProductAggregate product =
                    productService.queryProductById(request.getProductId());
            if (product == null) {
                return Response.<DirectOrderTrialResponse>builder()
                        .code(ResponseCode.NOT_FOUND.getCode())
                        .info("商品不存在")
                        .build();
            }
            BigDecimal originalPrice = product.getPrice();

            // 计算券后实付价
            BigDecimal payPrice = orderService.previewOrderDiscount(originalPrice, request.getCouponIds());
            BigDecimal deductionPrice = originalPrice.subtract(payPrice);

            DirectOrderTrialResponse data = DirectOrderTrialResponse.builder()
                    .productId(request.getProductId())
                    .originalPrice(originalPrice)
                    .deductionPrice(deductionPrice.compareTo(BigDecimal.ZERO) > 0 ? deductionPrice : BigDecimal.ZERO)
                    .payPrice(payPrice)
                    .build();

            log.info("直购试算完成 productId:{} 原价:{} 优惠:{} 实付:{}", request.getProductId(),
                    originalPrice, data.getDeductionPrice(), payPrice);
            return Response.<DirectOrderTrialResponse>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(data)
                    .build();
        } catch (Exception e) {
            log.error("直购试算失败 productId:{}", request.getProductId(), e);
            return Response.<DirectOrderTrialResponse>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 确认下单：携带 lockId + productId → 创建订单 → 返回 payUrl
     * 订单归属用户取自登录态 userId（兼容请求体 userId 兜底）
     */
    @RequestMapping(value = "confirm_order", method = RequestMethod.POST)
    public Response<ConfirmOrderResponse> confirmOrder(@RequestBody ConfirmOrderRequest request) {
        log.info("确认下单开始 request:{}", request);
        try {
            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String userId = (String) httpRequest.getAttribute("userId");
            if (userId == null) userId = request.getUserId();
            if (userId == null || userId.trim().isEmpty()) {
                throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "无法识别用户身份，请重新登录");
            }
            if (request.getProductId() == null || request.getProductId().trim().isEmpty()) {
                throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品编号不能为空");
            }
            log.info("确认下单开始 userId:{} productId:{} lockId:{}", userId, request.getProductId(), request.getLockId());

            PayOrderEntity payOrderEntity = orderService.createOrder(userId, request.getProductId(), request.getLockId());

            return Response.<ConfirmOrderResponse>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(ConfirmOrderResponse.builder()
                            .outTradeNo(payOrderEntity.getOutTradeNo())
                            .payUrl(payOrderEntity.getPayUrl())
                            .build())
                    .build();
        } catch (AppException e) {
            log.error("确认下单失败 lockId:{}", request.getLockId(), e);
            throw e;
        } catch (Exception e) {
            log.error("确认下单失败 lockId:{}", request.getLockId(), e);
            return Response.<ConfirmOrderResponse>builder()
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
        String userId = null;
        try {
            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            userId = (String) httpRequest.getAttribute("userId");
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
            log.error("退单失败 userId:{} outTradeNo:{}", userId, request.getOutTradeNo(), e);
            return Response.<String>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 关闭待支付订单：校验归属+状态后关闭（CREATE / PAY_WAIT 可关）
     */
    @RequestMapping(value = "close_order", method = RequestMethod.POST)
    public Response<String> closeOrder(@RequestBody RefundOrderRequest request) {
        log.info("关单开始 request:{}", request);
        String userId = null;
        try {
            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            userId = (String) httpRequest.getAttribute("userId");
            if (userId == null) {
                userId = request.getUserId();
            }
            if (userId == null || userId.trim().isEmpty()) {
                return Response.<String>builder()
                        .code(ResponseCode.NO_LOGIN.getCode())
                        .info(ResponseCode.NO_LOGIN.getInfo())
                        .build();
            }
            String outTradeNo = request.getOutTradeNo();
            if (outTradeNo == null || outTradeNo.trim().isEmpty()) {
                return Response.<String>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            orderService.closeOrder(userId, outTradeNo.trim());

            log.info("关单完成 userId:{} outTradeNo:{}", userId, outTradeNo);
            return Response.<String>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data("订单已关闭")
                    .build();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("关单失败 userId:{} outTradeNo:{}", userId, request.getOutTradeNo(), e);
            return Response.<String>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 待支付订单再次拉起支付：校验归属+状态后，幂等重生成支付宝表单，返回 payUrl 跳转
     */
    @RequestMapping(value = "repay_order", method = RequestMethod.POST)
    public Response<Map<String, String>> repayOrder(@RequestBody RefundOrderRequest request) {
        log.info("再次支付开始 request:{}", request);
        String userId = null;
        try {
            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            userId = (String) httpRequest.getAttribute("userId");
            if (userId == null) {
                userId = request.getUserId();
            }
            if (userId == null || userId.trim().isEmpty()) {
                return Response.<Map<String, String>>builder()
                        .code(ResponseCode.NO_LOGIN.getCode())
                        .info(ResponseCode.NO_LOGIN.getInfo())
                        .build();
            }
            String outTradeNo = request.getOutTradeNo();
            if (outTradeNo == null || outTradeNo.trim().isEmpty()) {
                return Response.<Map<String, String>>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            PayOrderEntity payOrderEntity = orderService.repayOrder(userId, outTradeNo.trim());

            Map<String, String> data = new HashMap<>();
            data.put("outTradeNo", payOrderEntity.getOutTradeNo());
            data.put("payUrl", payOrderEntity.getPayUrl());

            log.info("再次支付跳转完成 userId:{} outTradeNo:{}", userId, outTradeNo);
            return Response.<Map<String, String>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(data)
                    .build();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("再次支付失败 userId:{} outTradeNo:{}", userId, request.getOutTradeNo(), e);
            return Response.<Map<String, String>>builder()
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
