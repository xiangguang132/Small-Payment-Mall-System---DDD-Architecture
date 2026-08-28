package cn.bugstack.trigger.http;

import cn.bugstack.api.request.groupbuy.GroupBuyActivityMarketPlanRequest;
import cn.bugstack.api.request.groupbuy.GroupBuyExitRequest;
import cn.bugstack.api.request.groupbuy.GroupBuyLockOrderRequest;
import cn.bugstack.api.request.groupbuy.GroupBuyOrderPageRequest;
import cn.bugstack.api.request.groupbuy.GroupBuyRepayRequest;
import cn.bugstack.api.request.groupbuy.GroupBuyTrialRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.groupbuy.GroupBuyActivityMarketPlanResponse;
import cn.bugstack.api.response.groupbuy.GroupBuyLockOrderResponse;
import cn.bugstack.api.response.groupbuy.GroupBuyOrderDetailResponse;
import cn.bugstack.api.response.groupbuy.GroupBuyTrialRuleDetailResponse;
import cn.bugstack.api.response.groupbuy.GroupBuyTrialResponse;
import cn.bugstack.api.response.page.PageResponse;
import cn.bugstack.domain.auth.service.IUserProfileService;
import cn.bugstack.domain.groupbuy.model.aggregate.GroupBuyOrderAggregate;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyOrderEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderBehaviorEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderCommandEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialResult;
import cn.bugstack.domain.groupbuy.model.valobj.GroupBuyOrderDisplayStatusVO;
import cn.bugstack.domain.groupbuy.model.valobj.GroupBuyOrderStatusEnumVO;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyActivityRepository;
import cn.bugstack.domain.groupbuy.service.order.IGroupBuyOrderService;
import cn.bugstack.domain.groupbuy.service.refund.IGroupBuyRefundOrderService;
import cn.bugstack.domain.groupbuy.service.trial.IGroupBuyTrialService;
import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.service.IOrderService;
import cn.bugstack.trigger.interceptor.PublicEndpoint;
import cn.bugstack.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.Queue;
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
@RequestMapping("/api/v1/groupbuy/")
public class GroupBuyController {

    @Resource
    private IGroupBuyTrialService groupBuyTrialService;
    @Resource
    private IGroupBuyOrderService groupBuyOrderService;
    @Resource
    private IGroupBuyRefundOrderService groupBuyRefundOrderService;
    @Resource
    private IOrderService orderService;
    @Resource
    private IUserProfileService userProfileService;
    @Resource
    private IGroupBuyActivityRepository groupBuyActivityRepository;

    /**
     * 拼团试算：查询活动、商品与折扣，试算出折后价
     */
    @PublicEndpoint
    @RequestMapping(value = "queryGroupBuyTrial", method = RequestMethod.POST)
    public Response<GroupBuyTrialResponse> queryGroupBuyTrial(@RequestBody GroupBuyTrialRequest request) {
        log.info("拼团试算开始 request:{}", request);
        try {
            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String userId = (String) httpRequest.getAttribute("userId");
            if (userId == null) userId = request.getUserId();

            GroupBuyTrialResult trialResult = groupBuyTrialService.queryGroupBuyTrial(
                    cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialRequest.builder()
                            .userId(userId)
                            .activityId(request.getActivityId())
                            .productId(request.getProductId())
                            .couponIds(request.getCouponIds())
                            .usePoints(request.getUsePoints())
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
                    .ruleDetails(trialResult.getRuleDetails() == null ? null : trialResult.getRuleDetails().stream()
                            .map(rule -> GroupBuyTrialRuleDetailResponse.builder()
                                    .ruleType(rule.getRuleType() == null ? null : rule.getRuleType().name())
                                    .ruleCode(rule.getRuleCode())
                                    .ruleName(rule.getRuleName())
                                    .originalPrice(rule.getOriginalPrice())
                                    .currentPrice(rule.getCurrentPrice())
                                    .deductionPrice(rule.getDeductionPrice())
                                    .payPrice(rule.getPayPrice())
                                    .stackable(rule.getStackable())
                                    .matched(rule.getMatched())
                                    .message(rule.getMessage())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();

            log.info("拼团试算完成 userId:{} activityId:{} payPrice:{}", userId, request.getActivityId(), data.getPayPrice());
            return Response.<GroupBuyTrialResponse>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(data)
                    .build();
        } catch (Exception e) {
            log.error("拼团试算失败 userId:{} activityId:{} productId:{}", request.getUserId(), request.getActivityId(), request.getProductId(), e);
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
        try {
            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String userId = (String) httpRequest.getAttribute("userId");

            // 身份必须来自 JWT（AuthInterceptor 注入），禁止回退到请求体透传，防止越权
            if (StringUtils.isBlank(userId)) {
                log.warn("拼团锁单缺少登录态，拒绝处理");
                return Response.<GroupBuyLockOrderResponse>builder()
                        .code(ResponseCode.NO_LOGIN.getCode())
                        .info(ResponseCode.NO_LOGIN.getInfo())
                        .build();
            }

            // 硬限制：账号资料未完善（未绑定手机号）不允许锁单
            if (!userProfileService.isProfileCompleted(userId)) {
                log.info("拼团锁单拦截：账号资料未完善 userId:{}", userId);
                return Response.<GroupBuyLockOrderResponse>builder()
                        .code(ResponseCode.E0201.getCode())
                        .info(ResponseCode.E0201.getInfo())
                        .build();
            }

            // 1. 内部试算，取 payPrice/targetCount/validTime
            GroupBuyTrialResult trialResult = groupBuyTrialService.queryGroupBuyTrial(
                    cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialRequest.builder()
                            .userId(userId)
                            .activityId(request.getActivityId())
                            .productId(request.getProductId())
                            .couponIds(request.getCouponIds())
                            .build()
            );

            log.info("【价格流转】锁单试算结果 userId:{} 原价:{} 优惠减免:{} 实付价:{} 使用优惠券:{}",
                    userId, trialResult.getOriginalPrice(), trialResult.getDeductionPrice(),
                    trialResult.getPayPrice(), request.getCouponIds());

            // 2. 组装锁单聚合；outTradeNo 与支付单共用（回调结算按此反查拼团订单）
            // 幂等号：优先使用前端透传的 outTradeNo（重试传同一值），为空则服务端生成
            String outTradeNo = StringUtils.isBlank(request.getOutTradeNo())
                    ? RandomStringUtils.randomNumeric(14)
                    : request.getOutTradeNo().trim();
            GroupBuyOrderAggregate aggregate = GroupBuyOrderAggregate.builder()
                    .userId(userId)
                    .trialResult(trialResult)
                    .teamId(request.getTeamId())
                    .source(request.getSource())
                    .channel(request.getChannel())
                    .outTradeNo(outTradeNo)
                    .couponIds(request.getCouponIds())
                    .build();

            // 3. 幂等锁单，返回拼团订单（含 teamId/payAmount）；复用已有订单时 outTradeNo 以订单为准
            GroupBuyOrderEntity groupBuyOrderEntity = groupBuyOrderService.lockGroupBuyOrder(aggregate);

            // 4. 创建 GROUP_BUY 支付单，金额为拼团实付价；outTradeNo 必须与拼团订单一致，否则回调无法反查结算
            PayOrderEntity payOrderEntity = orderService.createGroupBuyPayOrder(
                    userId,
                    String.valueOf(groupBuyOrderEntity.getProductId()),
                    groupBuyOrderEntity.getProductName(),
                    groupBuyOrderEntity.getOutTradeNo(),
                    groupBuyOrderEntity.getPayAmount()
            );
            outTradeNo = groupBuyOrderEntity.getOutTradeNo();

            log.info("拼团锁单完成 userId:{} teamId:{} outTradeNo:{} payAmount:{}", userId,
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
            log.error("拼团锁单失败 userId:{} activityId:{} productId:{}", request.getUserId(), request.getActivityId(), request.getProductId(), e);
            return Response.<GroupBuyLockOrderResponse>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 待付款拼团订单再次拉起支付：校验本人订单仍为 LOCKED(0) 后，
     * 复用 createGroupBuyPayOrder 幂等重生成支付宝表单（金额取订单原 payAmount），返回结构与锁单一致
     */
    @RequestMapping(value = "repayGroupBuyOrder", method = RequestMethod.POST)
    public Response<GroupBuyLockOrderResponse> repayGroupBuyOrder(@RequestBody GroupBuyRepayRequest request) {
        log.info("拼团再次支付开始 outTradeNo:{}", request.getOutTradeNo());
        String userId = null;
        try {
            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            userId = (String) httpRequest.getAttribute("userId");

            if (StringUtils.isBlank(userId)) {
                log.warn("拼团再次支付缺少登录态，拒绝处理");
                return Response.<GroupBuyLockOrderResponse>builder()
                        .code(ResponseCode.NO_LOGIN.getCode())
                        .info(ResponseCode.NO_LOGIN.getInfo())
                        .build();
            }

            if (StringUtils.isBlank(request.getOutTradeNo())) {
                log.warn("拼团再次支付缺少外部交易单号 userId:{}", userId);
                return Response.<GroupBuyLockOrderResponse>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            // 归属与状态校验：仅本人的待付款(LOCKED)订单可再次拉起支付
            GroupBuyOrderEntity order = groupBuyOrderService.queryByUserIdAndOutTradeNo(
                    userId, request.getOutTradeNo().trim());
            if (order == null) {
                return Response.<GroupBuyLockOrderResponse>builder()
                        .code(ResponseCode.NOT_FOUND.getCode())
                        .info("订单不存在")
                        .build();
            }
            if (!GroupBuyOrderStatusEnumVO.LOCKED.getCode().equals(order.getStatus())) {
                log.info("拼团再次支付拦截：订单状态非待付款 userId:{} outTradeNo:{} status:{}",
                        userId, order.getOutTradeNo(), order.getStatus());
                return Response.<GroupBuyLockOrderResponse>builder()
                        .code(ResponseCode.UN_ERROR.getCode())
                        .info("订单状态已变化，请刷新后重试")
                        .build();
            }

            // 幂等重生成支付表单：已存在支付单仅回写 payUrl 并保持 PAY_WAIT；金额/商品以订单为准
            PayOrderEntity payOrderEntity = orderService.createGroupBuyPayOrder(
                    userId,
                    String.valueOf(order.getProductId()),
                    order.getProductName(),
                    order.getOutTradeNo(),
                    order.getPayAmount()
            );

            log.info("拼团再次支付跳转完成 userId:{} teamId:{} outTradeNo:{}", userId, order.getTeamId(), order.getOutTradeNo());
            return Response.<GroupBuyLockOrderResponse>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(GroupBuyLockOrderResponse.builder()
                            .teamId(order.getTeamId())
                            .outTradeNo(payOrderEntity.getOutTradeNo())
                            .payUrl(payOrderEntity.getPayUrl())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("拼团再次支付异常 userId:{} outTradeNo:{}", userId, request.getOutTradeNo(), e);
            return Response.<GroupBuyLockOrderResponse>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 退出拼团：按 outTradeNo 取消本人拼团订单（未支付仅取消；已支付走真实支付宝退款），
     * 复用退单链（幂等防重/状态路由/回调）；若为团内最后一人，原子关闭队伍
     */
    @RequestMapping(value = "exitGroupBuyOrder", method = RequestMethod.POST)
    public Response<String> exitGroupBuyOrder(@RequestBody GroupBuyExitRequest request) {
        log.info("退出拼团开始 outTradeNo:{}", request.getOutTradeNo());
        String userId = null;
        try {
            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            userId = (String) httpRequest.getAttribute("userId");

            if (StringUtils.isBlank(userId)) {
                log.warn("退出拼团缺少登录态，拒绝处理");
                return Response.<String>builder()
                        .code(ResponseCode.NO_LOGIN.getCode())
                        .info(ResponseCode.NO_LOGIN.getInfo())
                        .build();
            }

            if (StringUtils.isBlank(request.getOutTradeNo())) {
                log.warn("退出拼团缺少外部交易单号 userId:{}", userId);
                return Response.<String>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            GroupBuyRefundOrderCommandEntity command = GroupBuyRefundOrderCommandEntity.builder()
                    .userId(userId)
                    .outTradeNo(request.getOutTradeNo().trim())
                    .refundType("userExitTeam")
                    .build();
            GroupBuyRefundOrderBehaviorEntity behaviorEntity = groupBuyRefundOrderService.refundGroupBuyOrder(command);

            if (behaviorEntity == null || !behaviorEntity.isSuccess()) {
                String message = behaviorEntity == null ? "退出拼团失败" : behaviorEntity.getMessage();
                log.warn("退出拼团失败 userId:{} outTradeNo:{} message:{}", userId, request.getOutTradeNo(), message);
                return Response.<String>builder()
                        .code(ResponseCode.UN_ERROR.getCode())
                        .info(message)
                        .build();
            }

            log.info("退出拼团跳转页面完成 userId:{} teamId:{} message:{}",
                    userId, behaviorEntity.getTeamId(), behaviorEntity.getMessage());
            return Response.<String>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(behaviorEntity.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("退出拼团异常 userId:{} outTradeNo:{}", userId, request.getOutTradeNo(), e);
            return Response.<String>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 分页查询拼团订单列表，支持按展示态 status（10/20/30/40）和 userId 筛选，null=全部
     */
    @RequestMapping(value = "queryGroupBuyOrderPage", method = RequestMethod.POST)
    public Response<PageResponse<GroupBuyOrderDetailResponse>> queryGroupBuyOrderPage(@RequestBody GroupBuyOrderPageRequest request) {
        log.info("拼团订单分页查询开始 status:{} pageNo:{} pageSize:{}",
                request.getStatus(), request.getPageNo(), request.getPageSize());
        try {
            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String userId = (String) httpRequest.getAttribute("userId");

            // 身份必须来自 JWT；为空时直接拒绝，避免 mapper 丢失 user_id 过滤导致越权查询
            if (StringUtils.isBlank(userId)) {
                log.warn("拼团订单分页查询缺少登录态，拒绝处理");
                return Response.<PageResponse<GroupBuyOrderDetailResponse>>builder()
                        .code(ResponseCode.NO_LOGIN.getCode())
                        .info(ResponseCode.NO_LOGIN.getInfo())
                        .build();
            }

            Integer status = request.getStatus();

            List<GroupBuyOrderEntity> orderList = groupBuyOrderService.queryPageByStatusAndUserId(
                    status, userId, request.getPageNo(), request.getPageSize());
            long total = groupBuyOrderService.countByStatusAndUserId(status, userId);

            List<GroupBuyOrderDetailResponse> detailList = orderList.stream()
                    .map(order -> GroupBuyOrderDetailResponse.builder()
                            .id(order.getId())
                            .orderId(order.getOrderId())
                            .userId(order.getUserId())
                            .teamId(order.getTeamId())
                            .activityId(order.getActivityId())
                            .productId(order.getProductId())
                            .productName(order.getProductName())
                            .quantity(order.getQuantity())
                            .source(order.getSource())
                            .channel(order.getChannel())
                            .originalAmount(order.getOriginalAmount())
                            .deductionAmount(order.getDeductionAmount())
                            .payAmount(order.getPayAmount())
                            .status(order.getStatus())
                            .teamStatus(order.getTeamStatus())
                            .displayStatus(GroupBuyOrderDisplayStatusVO
                                    .resolve(order.getStatus(), order.getTeamStatus()).getCode())
                            .outTradeNo(order.getOutTradeNo())
                            .validStartTime(order.getValidStartTime())
                            .validEndTime(order.getValidEndTime())
                            .createTime(order.getCreateTime())
                            .updateTime(order.getUpdateTime())
                            .build())
                    .collect(Collectors.toList());

            PageResponse<GroupBuyOrderDetailResponse> pageResponse = PageResponse.<GroupBuyOrderDetailResponse>builder()
                    .total(total)
                    .pageNo(request.getPageNo())
                    .pageSize(request.getPageSize())
                    .list(detailList)
                    .build();

            log.info("拼团订单分页查询完成 total:{}", total);
            return Response.<PageResponse<GroupBuyOrderDetailResponse>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(pageResponse)
                    .build();
        } catch (Exception e) {
            log.error("拼团订单分页查询失败", e);
            return Response.<PageResponse<GroupBuyOrderDetailResponse>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 分页查询拼团活动（按 marketPlan 过滤，联表 group_buy_discount）
     */
    @PublicEndpoint
    @RequestMapping(value = "queryGroupBuyActivityPageByMarketPlan", method = RequestMethod.POST)
    public Response<PageResponse<GroupBuyActivityMarketPlanResponse>> queryGroupBuyActivityPageByMarketPlan(
            @RequestBody GroupBuyActivityMarketPlanRequest request) {
        log.info("拼团活动分页查询开始 marketPlan:{} pageNo:{} pageSize:{}",
                request.getMarketPlan(), request.getPageNo(), request.getPageSize());
        try {
            int offset = request.offset();
            int limit = request.limit();

            List<GroupBuyActivityEntity> activityList = groupBuyActivityRepository.queryActivityPageByMarketPlan(
                    request.getMarketPlan(), offset, limit);
            long total = groupBuyActivityRepository.countActivityPageByMarketPlan(request.getMarketPlan());

            List<GroupBuyActivityMarketPlanResponse> responseList = activityList.stream()
                    .map(activity -> GroupBuyActivityMarketPlanResponse.builder()
                            .activityId(activity.getActivityId())
                            .activityName(activity.getActivityName())
                            .productId(activity.getProductId())
                            .discountId(activity.getDiscountId())
                            .discountName(activity.getDiscountName())
                            .marketPlan(activity.getMarketPlan())
                            .startTime(activity.getStartTime())
                            .endTime(activity.getEndTime())
                            .build())
                    .collect(Collectors.toList());

            PageResponse<GroupBuyActivityMarketPlanResponse> pageResponse = PageResponse.<GroupBuyActivityMarketPlanResponse>builder()
                    .total(total)
                    .pageNo(request.getPageNo())
                    .pageSize(request.getPageSize())
                    .list(responseList)
                    .build();

            log.info("拼团活动分页查询完成 marketPlan:{} total:{}", request.getMarketPlan(), total);
            return Response.<PageResponse<GroupBuyActivityMarketPlanResponse>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(pageResponse)
                    .build();
        } catch (Exception e) {
            log.error("拼团活动分页查询失败 marketPlan:{}", request.getMarketPlan(), e);
            return Response.<PageResponse<GroupBuyActivityMarketPlanResponse>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

}