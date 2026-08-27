package cn.bugstack.domain.groupbuy.service.refund;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderBehaviorEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderCommandEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundRestoreEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyTeamRepository;
import cn.bugstack.domain.groupbuy.service.refund.business.IGroupBuyRefundOrderStrategy;
import cn.bugstack.domain.groupbuy.service.refund.factory.GroupBuyRefundOrderRuleFilterFactory;
import cn.bugstack.types.design.framework.link.multilink.chain.BusinessLinkedList;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Service
public class GroupBuyRefundOrderService implements IGroupBuyRefundOrderService {

    @Resource(name = "groupBuyOrderRefundFilter")
    private BusinessLinkedList<GroupBuyRefundOrderCommandEntity, GroupBuyRefundOrderRuleFilterFactory.DynamicContext, GroupBuyRefundOrderBehaviorEntity> groupBuyRefundOrderRuleFilter;

    @Resource
    private Map<String, IGroupBuyRefundOrderStrategy> refundGroupBuyOrderStrategyMap;

    @Resource
    private IGroupBuyTeamRepository groupBuyTeamRepository;

    /**
     * 拼团退单
     * @param groupBuyRefundOrderCommandEntity
     * @return
     */
    @Override
    public GroupBuyRefundOrderBehaviorEntity refundGroupBuyOrder(GroupBuyRefundOrderCommandEntity groupBuyRefundOrderCommandEntity) {
        GroupBuyRefundOrderRuleFilterFactory.DynamicContext dynamicContext = GroupBuyRefundOrderRuleFilterFactory.DynamicContext.builder().build();
        GroupBuyRefundOrderBehaviorEntity behaviorEntity;
        try {
            behaviorEntity = groupBuyRefundOrderRuleFilter.apply(groupBuyRefundOrderCommandEntity, dynamicContext);
        } catch (Exception e) {
            return GroupBuyRefundOrderBehaviorEntity.builder()
                    .userId(groupBuyRefundOrderCommandEntity.getUserId())
                    .teamId(groupBuyRefundOrderCommandEntity.getTeamId())
                    .orderId(groupBuyRefundOrderCommandEntity.getOrderId())
                    .activityId(groupBuyRefundOrderCommandEntity.getActivityId())
                    .success(false)
                    .message("拼团退单过滤链执行失败")
                    .build();
        }
        if (behaviorEntity == null) {
            return GroupBuyRefundOrderBehaviorEntity.builder()
                    .userId(groupBuyRefundOrderCommandEntity.getUserId())
                    .teamId(groupBuyRefundOrderCommandEntity.getTeamId())
                    .orderId(groupBuyRefundOrderCommandEntity.getOrderId())
                    .activityId(groupBuyRefundOrderCommandEntity.getActivityId())
                    .success(false)
                    .message(dynamicContext.getMessage())
                    .build();
        }
        String strategyName = behaviorEntity.getStrategyName();
        if (strategyName == null || strategyName.isEmpty()) {
            return behaviorEntity;
        }
        IGroupBuyRefundOrderStrategy strategy = refundGroupBuyOrderStrategyMap.get(strategyName);
        if (strategy == null) {
            log.warn("未找到拼团退单策略 strategyName:{}", strategyName);
            return GroupBuyRefundOrderBehaviorEntity.builder()
                    .userId(groupBuyRefundOrderCommandEntity.getUserId())
                    .teamId(groupBuyRefundOrderCommandEntity.getTeamId())
                    .orderId(groupBuyRefundOrderCommandEntity.getOrderId())
                    .activityId(groupBuyRefundOrderCommandEntity.getActivityId())
                    .success(false)
                    .message("未找到退单策略")
                    .build();
        }
        GroupBuyRefundOrderEntity refundOrderEntity = GroupBuyRefundOrderEntity.builder()
                .userId(behaviorEntity.getUserId())
                .teamId(behaviorEntity.getTeamId())
                .activityId(behaviorEntity.getActivityId())
                .orderId(behaviorEntity.getOrderId())
                .outTradeNo(behaviorEntity.getOutTradeNo())
                .payAmount(behaviorEntity.getPayAmount())
                .build();
        strategy.refundGroupBuyOrder(refundOrderEntity);
        closeTeamIfEmpty(behaviorEntity.getTeamId());
        behaviorEntity.setStrategyName(null);
        return behaviorEntity;
    }

    /**
     * 团空即关：退单成功后，若团内已无有效订单（锁定/已支付），原子关闭队伍。
     * 覆盖用户主动退出、超时退款等所有退单路径；失败仅记日志，不影响退单结果。
     */
    private void closeTeamIfEmpty(String teamId) {
        if (StringUtils.isBlank(teamId)) {
            return;
        }
        try {
            int closed = groupBuyTeamRepository.updateStatus2CloseIfEmpty(teamId);
            if (closed == 1) {
                log.info("拼团队伍内已无有效成员，队伍关闭 teamId:{}", teamId);
            }
        } catch (Exception e) {
            log.error("拼团队伍关闭检查失败 teamId:{}", teamId, e);
        }
    }

    @Override
    public void restoreTeamLockStock(GroupBuyRefundRestoreEntity restoreEntity) throws Exception {
        log.info("逆向流程，恢复锁单量 userId:{} activityId:{} teamId:{} refundType:{}",
                restoreEntity.getUserId(), restoreEntity.getActivityId(), restoreEntity.getTeamId(), restoreEntity.getRefundType());
        IGroupBuyRefundOrderStrategy strategy = refundGroupBuyOrderStrategyMap.get(restoreEntity.getRefundType());
        if (strategy == null) {
            log.warn("未找到恢复锁单量策略 refundType:{}", restoreEntity.getRefundType());
            throw new AppException(ResponseCode.UN_ERROR, "未找到恢复锁单量策略:" + restoreEntity.getRefundType());
        }
        strategy.reverseStock(restoreEntity);
    }
}
