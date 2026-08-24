package cn.bugstack.trigger.job;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderBehaviorEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderCommandEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyOrderRepository;
import cn.bugstack.domain.groupbuy.service.refund.IGroupBuyRefundOrderService;
import cn.bugstack.domain.timeout.ITimeoutOrderTaskProvider;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 拼团退单
 */
@Component
public class GroupBuyRefundTimeoutTaskProvider implements ITimeoutOrderTaskProvider {

    @Resource
    private IGroupBuyOrderRepository groupBuyOrderRepository;

    @Resource
    private IGroupBuyRefundOrderService groupBuyRefundOrderService;

    @Override
    public String taskName() {
        return "group-buy-refund";
    }

    @Override
    public List<String> queryTimeoutOutTradeNoList() {
        return groupBuyOrderRepository.queryTimeOutRefundOrderList();
    }

    @Override
    public boolean handle(String outTradeNo) {
        GroupBuyRefundOrderCommandEntity command =
                GroupBuyRefundOrderCommandEntity.builder()
                        .outTradeNo(outTradeNo)
                        .build();

        GroupBuyRefundOrderBehaviorEntity result =
                groupBuyRefundOrderService.refundGroupBuyOrder(command);
        return result != null && result.isSuccess();
    }
}
