package cn.bugstack.trigger.http;

import cn.bugstack.api.request.order.PayOrderPageRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.order.PayOrderDetailResponse;
import cn.bugstack.api.response.page.PageResponse;
import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.service.IOrderService;
import cn.bugstack.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/payorder/")
public class PayOrderController {

    @Resource
    private IOrderService orderService;

    /**
     * 分页查询用户支付订单列表，支持按订单状态筛选，null=全部
     * 状态值：CREATE-创建完成、PAY_WAIT-等待支付、PAY_SUCCESS-支付成功、DEAL_DONE-交易完成、CLOSE-订单关单、REFUNDING-退款中、REFUND-已退款
     */
    @RequestMapping(value = "queryPayOrderPage", method = RequestMethod.POST)
    public Response<PageResponse<PayOrderDetailResponse>> queryPayOrderPage(@RequestBody PayOrderPageRequest request) {
        log.info("支付订单分页查询开始 status:{} pageNo:{} pageSize:{}",
                request.getStatus(), request.getPageNo(), request.getPageSize());
        try {
            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String userId = (String) httpRequest.getAttribute("userId");

            // 身份必须来自 JWT；为空时直接拒绝，避免 mapper 丢失 user_id 过滤导致越权查询
            if (StringUtils.isBlank(userId)) {
                log.warn("支付订单分页查询缺少登录态，拒绝处理");
                return Response.<PageResponse<PayOrderDetailResponse>>builder()
                        .code(ResponseCode.NO_LOGIN.getCode())
                        .info(ResponseCode.NO_LOGIN.getInfo())
                        .build();
            }

            String status = request.getStatus();

            List<PayOrderEntity> orderList = orderService.queryPageByStatusAndUserId(
                    status, userId, request.getPageNo(), request.getPageSize());
            long total = orderService.countByStatusAndUserId(status, userId);

            List<PayOrderDetailResponse> detailList = orderList.stream()
                    .map(order -> PayOrderDetailResponse.builder()
                            .id(order.getId())
                            .userId(order.getUserId())
                            .productId(order.getProductId())
                            .productName(order.getProductName())
                            .outTradeNo(order.getOutTradeNo())
                            .orderTime(order.getOrderTime())
                            .totalAmount(order.getTotalAmount())
                            .orderType(order.getOrderType() == null ? null : order.getOrderType().getCode())
                            .status(order.getOrderStatus() == null ? null : order.getOrderStatus().getCode())
                            .payTime(order.getPayTime())
                            .outTradeTime(order.getOutTradeTime())
                            .createTime(order.getCreateTime())
                            .updateTime(order.getUpdateTime())
                            .build())
                    .collect(Collectors.toList());

            PageResponse<PayOrderDetailResponse> pageResponse = PageResponse.<PayOrderDetailResponse>builder()
                    .total(total)
                    .pageNo(request.getPageNo())
                    .pageSize(request.getPageSize())
                    .list(detailList)
                    .build();

            log.info("支付订单分页查询完成 total:{}", total);
            return Response.<PageResponse<PayOrderDetailResponse>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(pageResponse)
                    .build();
        } catch (Exception e) {
            log.error("支付订单分页查询失败", e);
            return Response.<PageResponse<PayOrderDetailResponse>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

}
