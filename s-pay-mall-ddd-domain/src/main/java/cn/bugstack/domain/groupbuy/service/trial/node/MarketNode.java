package cn.bugstack.domain.groupbuy.service.trial.node;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyDiscountEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialRequest;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialResult;
import cn.bugstack.domain.groupbuy.service.trial.AbstractGroupBuyMarketSupport;
import cn.bugstack.domain.groupbuy.service.trial.factory.DefaultActivityStrategyFactory;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class MarketNode extends AbstractGroupBuyMarketSupport {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private TagNode tagNode;

    @Resource
    private ErrorNode errorNode;

    // todo 引入多线程查询商品配置与商品信息


    @Override
    protected void multiThread(GroupBuyTrialRequest requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext)
            throws ExecutionException, InterruptedException, TimeoutException {
        // 异步获取活动配置 与 商品信息
        // 活动配置
        QueryGroupBuyActivityVOThreadTask queryGroupBuyActivityVOThreadTask = new QueryGroupBuyActivityVOThreadTask(
                requestParameter.getActivityId(),
                activityRepository
        );

        FutureTask<GroupBuyActivityEntity> groupBuyActivityEntityFutureTask = new FutureTask<>(queryGroupBuyActivityVOThreadTask);
        threadPoolExecutor.execute(groupBuyActivityEntityFutureTask);

        // 商品
        QueryProductVOFromDBThreadTask queryProductVOFromDBThreadTask = new QueryProductVOFromDBThreadTask(
                requestParameter.getProductId(),
                productRepository
        );
        FutureTask<ProductAggregate> productAggregateFutureTask = new FutureTask<>(queryProductVOFromDBThreadTask);
        threadPoolExecutor.execute(productAggregateFutureTask);

        // 写入上下文
        dynamicContext.setActivity(groupBuyActivityEntityFutureTask.get(timeout, TimeUnit.MILLISECONDS));
        dynamicContext.setProduct(productAggregateFutureTask.get(timeout, TimeUnit.MILLISECONDS));

        log.info("拼团商品查询 活动配置、商品 试算服务-MarketNode userId:{} 异步线程加载数据「GroupBuyActivityEntity、ProductAggregate」完成", requestParameter.getUserId());
    }

    /**
     * 作用：通过 dynamic 传递信息
     * @param requestParameter
     * @param dynamicContext
     * @return
     * @throws Exception
     */
    @Override
    public GroupBuyTrialResult doApply(GroupBuyTrialRequest requestParameter,DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {

        // 获取配置信息
        GroupBuyActivityEntity activityEntity = activityRepository.queryGroupBuyActivityByActivityId(requestParameter.getActivityId());
        if (activityEntity == null ) {
            throw new AppException(ResponseCode.NOT_FOUND);
        }
        // 获取折扣信息
        GroupBuyDiscountEntity discountEntity = discountRepository.queryDiscountById(activityEntity.getDiscountId());
        // 获取商品信息
        ProductAggregate productAggregate = productRepository.queryById(activityEntity.getProductId());

        dynamicContext.setActivity(activityEntity);
        dynamicContext.setDiscount(discountEntity);
        dynamicContext.setProduct(productAggregate);

        if (productAggregate == null) {
            throw new AppException(ResponseCode.NOT_FOUND);
        }
        dynamicContext.setOriginalPrice(productAggregate.getPrice());
        dynamicContext.setPayPrice(productAggregate.getPrice());
        dynamicContext.setDeductionPrice(BigDecimal.ZERO);

        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<GroupBuyTrialRequest,
                DefaultActivityStrategyFactory.DynamicContext, GroupBuyTrialResult>
    get(GroupBuyTrialRequest request,
        DefaultActivityStrategyFactory.DynamicContext dynamicContext) {
        if (dynamicContext.getActivity() == null
                || dynamicContext.getProduct() == null
                || dynamicContext.getDiscount() == null) {
            return errorNode;
        }
        return tagNode;
    }
}

