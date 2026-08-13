package cn.bugstack.domain.groupbuy.service.trial.node;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialRequest;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialResult;
import cn.bugstack.domain.groupbuy.service.trial.AbstractGroupBuyMarketSupport;
import cn.bugstack.domain.groupbuy.service.trial.factory.DefaultActivityStrategyFactory;
import cn.bugstack.types.design.framework.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class TagNode extends AbstractGroupBuyMarketSupport {

    @Resource
    private EndNode endNode;

    @Override
    protected GroupBuyTrialResult doApply(GroupBuyTrialRequest requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        GroupBuyActivityEntity activity = dynamicContext.getActivity();
        String tagId = activity == null ? null : activity.getTagId();

        if (StringUtils.isBlank(tagId)) {
            dynamicContext.setVisible(true);
            dynamicContext.setEnable(true);
            return router(requestParameter, dynamicContext);
        }

        String tagScope = activity == null ? null : activity.getTagScope();
        boolean withCrowd = activityRepository.withinTagCrowdRange(tagId, requestParameter.getUserId());

        boolean visibleAllowed = StringUtils.isBlank(tagScope) || !containsScope(tagScope, "1");
        boolean enableAllowed = StringUtils.isBlank(tagScope) || !containsScope(tagScope, "2");

        dynamicContext.setVisible(visibleAllowed || withCrowd);
        dynamicContext.setEnable(enableAllowed || withCrowd);

        return router(requestParameter, dynamicContext);
    }

    private boolean containsScope(String tagScope, String scope) {
        for (String item : tagScope.split(",")) {
            if (scope.equals(StringUtils.trim(item))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public StrategyHandler<GroupBuyTrialRequest, DefaultActivityStrategyFactory.DynamicContext, GroupBuyTrialResult> get(GroupBuyTrialRequest requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return endNode;
    }

}
