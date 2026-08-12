package cn.bugstack.domain.groupbuy.service.trial;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialRequest;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialResult;
import cn.bugstack.domain.groupbuy.service.trial.factory.DefaultActivityStrategyFactory;
import cn.bugstack.types.design.framework.tree.StrategyHandler;
import org.springframework.stereotype.Service;

@Service
public class GroupBuyTrialService implements IGroupBuyTrialService {

    private final DefaultActivityStrategyFactory factory;

    public GroupBuyTrialService(DefaultActivityStrategyFactory factory) {
        this.factory = factory;
    }

    @Override
    public GroupBuyTrialResult queryGroupBuyTrial(GroupBuyTrialRequest request) throws Exception {
        StrategyHandler<
                GroupBuyTrialRequest,
                DefaultActivityStrategyFactory.DynamicContext,
                GroupBuyTrialResult
                > handler = factory.strategyHandler();
        return handler.apply(request, new DefaultActivityStrategyFactory.DynamicContext());
    }
}
