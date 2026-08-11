package cn.bugstack.domain.groupbuy.service.trial;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialRequest;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialResult;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyActivityRepository;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyDiscountRepository;
import cn.bugstack.domain.groupbuy.service.trial.factory.DefaultActivityStrategyFactory;
import cn.bugstack.domain.product.repository.IProductRepository;
import cn.bugstack.types.design.framework.tree.AbstractMultiThreadStrategyRouter;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * 试算业务桥接器
 * 抽象的拼团营销支撑类
 */
public abstract class AbstractGroupBuyMarketSupport extends AbstractMultiThreadStrategyRouter<GroupBuyTrialRequest,
        DefaultActivityStrategyFactory.DynamicContext, GroupBuyTrialResult> {

    protected long timeout = 5000;

    @Resource
    protected IGroupBuyActivityRepository activityRepository;

    @Resource
    protected IGroupBuyDiscountRepository discountRepository;

    @Resource
    protected IProductRepository productRepository;

    @Override
    protected void multiThread(GroupBuyTrialRequest request,
                               DefaultActivityStrategyFactory.DynamicContext
                                       dynamicContext) {
        // 默认不异步，具体节点按需重写
    }
}
