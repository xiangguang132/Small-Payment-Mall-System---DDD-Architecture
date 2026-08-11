package cn.bugstack.domain.groupbuy.service.trial.node;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialRequest;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialResult;
import cn.bugstack.domain.groupbuy.service.trial.AbstractGroupBuyMarketSupport;
import cn.bugstack.domain.groupbuy.service.trial.factory.DefaultActivityStrategyFactory;
import cn.bugstack.types.design.framework.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class SwitchNode extends AbstractGroupBuyMarketSupport {

    @Resource
    private MarketNode marketNode;

    @Override
    public GroupBuyTrialResult doApply(GroupBuyTrialRequest requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {


        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<GroupBuyTrialRequest, DefaultActivityStrategyFactory.DynamicContext, GroupBuyTrialResult> get(GroupBuyTrialRequest requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return marketNode;
    }
}
