package cn.bugstack.domain.groupbuy.service.trial;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialRequest;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialResult;

public interface IGroupBuyTrialService {

    GroupBuyTrialResult queryGroupBuyTrial(GroupBuyTrialRequest request) throws Exception;

}
