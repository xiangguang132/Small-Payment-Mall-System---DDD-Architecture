package cn.bugstack.domain.groupbuy.service.trial.node;

import cn.bugstack.domain.groupbuy.model.entity.*;
import cn.bugstack.domain.groupbuy.repository.ICouponRepository;
import cn.bugstack.domain.groupbuy.repository.IPointsRepository;
import cn.bugstack.domain.groupbuy.service.discount.IGroupBuyDiscountService;
import cn.bugstack.domain.groupbuy.service.trial.AbstractGroupBuyMarketSupport;
import cn.bugstack.domain.groupbuy.service.trial.factory.DefaultActivityStrategyFactory;
import cn.bugstack.domain.groupbuy.service.trial.rule.TrialRuleChain;
import cn.bugstack.domain.groupbuy.service.trial.rule.TrialRuleContext;
import cn.bugstack.domain.groupbuy.service.trial.thread.QueryGroupBuyActivityVOThreadTask;
import cn.bugstack.domain.groupbuy.service.trial.thread.QueryProductVOFromDBThreadTask;
import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import cn.bugstack.types.design.framework.tree.StrategyHandler;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MarketNode extends AbstractGroupBuyMarketSupport {

    @Resource
    private TrialRuleChain trialRuleChain;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private ICouponRepository couponRepository;
    @Resource
    private IPointsRepository pointsRepository;
    @Resource
    private TagNode tagNode;
    @Resource
    private ErrorNode errorNode;
    @Resource
    private Map<String, IGroupBuyDiscountService> discountServiceMap;

    @Override
    protected void multiThread(GroupBuyTrialRequest requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext)
            throws ExecutionException, InterruptedException, TimeoutException {
        QueryGroupBuyActivityVOThreadTask queryGroupBuyActivityVOThreadTask = new QueryGroupBuyActivityVOThreadTask(
                requestParameter.getActivityId(),
                activityRepository
        );

        FutureTask<GroupBuyActivityEntity> groupBuyActivityEntityFutureTask = new FutureTask<>(queryGroupBuyActivityVOThreadTask);
        threadPoolExecutor.execute(groupBuyActivityEntityFutureTask);

        QueryProductVOFromDBThreadTask queryProductVOFromDBThreadTask = new QueryProductVOFromDBThreadTask(
                requestParameter.getProductId(),
                productRepository
        );
        FutureTask<ProductAggregate> productAggregateFutureTask = new FutureTask<>(queryProductVOFromDBThreadTask);
        threadPoolExecutor.execute(productAggregateFutureTask);

        dynamicContext.setActivity(groupBuyActivityEntityFutureTask.get(timeout, TimeUnit.MILLISECONDS));
        dynamicContext.setProduct(productAggregateFutureTask.get(timeout, TimeUnit.MILLISECONDS));

        log.info("拼团商品查询 活动配置、商品 试算服务-MarketNode userId:{} 异步线程加载数据「GroupBuyActivityEntity、ProductAggregate」完成", requestParameter.getUserId());
    }

    @Override
    public GroupBuyTrialResult doApply(GroupBuyTrialRequest requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        ProductAggregate product = productRepository.queryById(requestParameter.getProductId());
        if (product == null) {
            throw new AppException(ResponseCode.NOT_FOUND);
        }
        BigDecimal originalPrice = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();

        GroupBuyActivityEntity activity = null;
        GroupBuyDiscountEntity discount = null;
        if (requestParameter.getActivityId() != null) {
            activity = activityRepository.queryGroupBuyActivityByActivityId(requestParameter.getActivityId());
            if (activity != null) {
                discount = discountRepository.queryDiscountById(activity.getDiscountId());
            }
        }

        List<CouponEntity> selectedCoupons = Collections.emptyList();
        if (requestParameter.getCouponIds() != null && !requestParameter.getCouponIds().isEmpty()) {
            selectedCoupons = requestParameter.getCouponIds().stream()
                    .map(couponRepository::queryCouponByCouponId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        PointsEntity points = null;
        if (Boolean.TRUE.equals(requestParameter.getUsePoints())) {
            points = pointsRepository.queryPointsByUserId(requestParameter.getUserId());
        }

        TrialRuleContext trialContext = TrialRuleContext.builder()
                .userId(requestParameter.getUserId())
                .productId(requestParameter.getProductId())
                .product(product)
                .activity(activity)
                .discount(discount)
                .originalPrice(originalPrice)
                .currentPrice(originalPrice)
                .selectedCoupons(selectedCoupons)
                .selectedCouponIds(requestParameter.getCouponIds())
                .points(points)
                .build();

        TrialRuleContext resultContext = trialRuleChain.execute(trialContext);

        log.info("【价格流转】试算完成 userId:{} productId:{} 原价:{} 优惠减免:{} 实付价:{}",
                requestParameter.getUserId(), requestParameter.getProductId(),
                originalPrice, originalPrice.subtract(resultContext.getCurrentPrice()), resultContext.getCurrentPrice());

        dynamicContext.setActivity(activity);
        dynamicContext.setDiscount(discount);
        dynamicContext.setProduct(product);
        dynamicContext.setOriginalPrice(originalPrice);
        dynamicContext.setPayPrice(resultContext.getCurrentPrice());
        dynamicContext.setDeductionPrice(originalPrice.subtract(resultContext.getCurrentPrice()));
        dynamicContext.setAppliedRuleResults(resultContext.getAppliedRuleResults());

        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<GroupBuyTrialRequest, DefaultActivityStrategyFactory.DynamicContext, GroupBuyTrialResult> get(
            GroupBuyTrialRequest request,
            DefaultActivityStrategyFactory.DynamicContext dynamicContext) {
        if (dynamicContext.getProduct() == null) {
            return errorNode;
        }
        return tagNode;
    }
}