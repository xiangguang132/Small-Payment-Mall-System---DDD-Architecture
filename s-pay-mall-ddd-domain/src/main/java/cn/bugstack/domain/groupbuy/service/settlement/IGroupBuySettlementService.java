package cn.bugstack.domain.groupbuy.service.settlement;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuySettlementCommandEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuySettlementFeedBackEntity;
import cn.bugstack.domain.groupbuy.model.valobj.GroupBuyProgressVO;

public interface IGroupBuySettlementService {

    GroupBuySettlementFeedBackEntity settlementGroupBuyOrder(
            GroupBuySettlementCommandEntity command) throws Exception;

    GroupBuyProgressVO queryGroupBuyProgress(String teamId);

}
