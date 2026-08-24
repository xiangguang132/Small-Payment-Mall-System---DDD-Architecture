package cn.bugstack.domain.groupbuy.service.refund;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderBehaviorEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderCommandEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class GroupBuyRefundOrderService implements IGroupBuyRefundOrderService {
    /**
     * 拼团退单
     * @param groupBuyRefundOrderCommandEntity
     * @return
     */
    @Override
    public GroupBuyRefundOrderBehaviorEntity refundGroupBuyOrder(GroupBuyRefundOrderCommandEntity groupBuyRefundOrderCommandEntity) {
        return null;
    }
}
