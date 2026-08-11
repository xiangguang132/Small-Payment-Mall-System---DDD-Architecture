package cn.bugstack.domain.groupbuy.service.trial.node;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialRequest;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialResult;
import cn.bugstack.domain.groupbuy.service.trial.AbstractGroupBuyMarketSupport;
import cn.bugstack.domain.groupbuy.service.trial.factory.DefaultActivityStrategyFactory;
import cn.bugstack.types.design.framework.tree.StrategyHandler;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class RootNode extends AbstractGroupBuyMarketSupport {

    @Resource
    private SwitchNode switchNode;

    @Override
    public GroupBuyTrialResult doApply(GroupBuyTrialRequest requestParameter,DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        // 参数校验
        if (requestParameter == null || requestParameter.getActivityId() == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "拼团试算参数不能为空");
        }

        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<GroupBuyTrialRequest,
                DefaultActivityStrategyFactory.DynamicContext, GroupBuyTrialResult>
    get(GroupBuyTrialRequest request,
        DefaultActivityStrategyFactory.DynamicContext dynamicContext) {
        return switchNode;
    }

}
