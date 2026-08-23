package cn.bugstack.trigger.http;

import cn.bugstack.api.request.groupbuy.GroupBuyLockOrderRequest;
import cn.bugstack.api.request.groupbuy.GroupBuyTrialRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.groupbuy.GroupBuyLockOrderResponse;
import cn.bugstack.api.response.groupbuy.GroupBuyTrialResponse;
import cn.bugstack.domain.groupbuy.model.aggregate.GroupBuyOrderAggregate;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyOrderEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialResult;
import cn.bugstack.domain.groupbuy.service.order.IGroupBuyOrderService;
import cn.bugstack.domain.groupbuy.service.trial.IGroupBuyTrialService;
import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.service.IOrderService;
import cn.bugstack.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/groupbuy/")
public class GroupBuyController {

    @Resource
    private IGroupBuyTrialService groupBuyTrialService;
    @Resource
    private IGroupBuyOrderService groupBuyOrderService;
    @Resource
    private IOrderService orderService;

    /**
     * 拼团试算：查询活动、商品与折扣，试算出折后价
     */
    @RequestMapping(value = "queryGroupBuyTrial", method = RequestMethod.POST)
    public Response<GroupBuyTrialResponse> queryGroupBuyTrial(@RequestBody GroupBuyTrialRequest request) {
        log.info("拼团试算开始 request:{}", request);
        String openid = null;
        try {
            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String userId = (String) httpRequest.getAttribute("openid");
            openid = userId;
            if (userId == null) userId = request.getUserId();

            GroupBuyTrialResult trialResult = groupBuyTrialService.queryGroupBuyTrial(
                    cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialRequest.builder()
                            .userId(userId)
                            .activityId(request.getActivityId())
                            .productId(request.getProductId())
                            .build()
            );

            GroupBuyTrialResponse data = GroupBuyTrialResponse.builder()
                    .activityId(trialResult.getActivityId())
                    .activityName(trialResult.getActivityName())
                    .targetCount(trialResult.getTargetCount())
                    .validTime(trialResult.getValidTime())
                    .productId(trialResult.getProductId())
                    .productName(trialResult.getProductName())
                    .originalPrice(trialResult.getOriginalPrice())
                    .deductionPrice(trialResult.getDeductionPrice())
                    .payPrice(trialResult.getPayPrice())
                    .visible(trialResult.getVisible())
                    .enable(trialResult.getEnable())
                    .build();

            log.info("拼团试算完成 userId:{} activityId:{} payPrice:{}", openid, request.getActivityId(), data.getPayPrice());
            return Response.<GroupBuyTrialResponse>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(data)
                    .build();
        } catch (Exception e) {
            log.error("拼团试算失败 userId:{} activityId:{} productId:{}", openid, request.getActivityId(), request.getProductId(), e);
            return Response.<GroupBuyTrialResponse>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 拼团锁单+支付一步到位：内部试算 → 锁单 → 创建 GROUP_BUY 支付单，返回支付宝支付表单
     */
    @RequestMapping(value = "lockGroupBuyOrder", method = RequestMethod.POST)
    public Response<GroupBuyLockOrderResponse> lockGroupBuyOrder(@RequestBody GroupBuyLockOrderRequest request) {
        log.info("拼团锁单开始 request:{}", request);
        String openid = null;
        try {
            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String userId = (String) httpRequest.getAttribute("openid");
            openid = userId;
            if (userId == null) userId = request.getUserId();

            // 1. 内部试算，取 payPrice/targetCount/validTime
            GroupBuyTrialResult trialResult = groupBuyTrialService.queryGroupBuyTrial(
                    cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialRequest.builder()
                            .userId(userId)
                            .activityId(request.getActivityId())
                            .productId(request.getProductId())
                            .build()
            );

            // 2. 组装锁单聚合；outTradeNo 与支付单共用（回调结算按此反查拼团订单）
            String outTradeNo = RandomStringUtils.randomNumeric(14);
            GroupBuyOrderAggregate aggregate = GroupBuyOrderAggregate.builder()
                    .userId(userId)
                    .trialResult(trialResult)
                    .teamId(request.getTeamId())
                    .source(request.getSource())
                    .channel(request.getChannel())
                    .outTradeNo(outTradeNo)
                    .build();

            // 3. 幂等锁单，返回拼团订单（含 teamId/payAmount）
            GroupBuyOrderEntity groupBuyOrderEntity = groupBuyOrderService.lockGroupBuyOrder(aggregate);

            // 4. 创建 GROUP_BUY 支付单，金额为拼团实付价
            PayOrderEntity payOrderEntity = orderService.createGroupBuyPayOrder(
                    userId,
                    String.valueOf(groupBuyOrderEntity.getProductId()),
                    groupBuyOrderEntity.getProductName(),
                    outTradeNo,
                    groupBuyOrderEntity.getPayAmount()
            );

            log.info("拼团锁单完成 userId:{} teamId:{} outTradeNo:{} payAmount:{}", openid,
                    groupBuyOrderEntity.getTeamId(), outTradeNo, groupBuyOrderEntity.getPayAmount());
            return Response.<GroupBuyLockOrderResponse>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(GroupBuyLockOrderResponse.builder()
                            .teamId(groupBuyOrderEntity.getTeamId())
                            .outTradeNo(payOrderEntity.getOutTradeNo())
                            .payUrl(payOrderEntity.getPayUrl())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("拼团锁单失败 userId:{} activityId:{} productId:{}", openid, request.getActivityId(), request.getProductId(), e);
            return Response.<GroupBuyLockOrderResponse>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

}