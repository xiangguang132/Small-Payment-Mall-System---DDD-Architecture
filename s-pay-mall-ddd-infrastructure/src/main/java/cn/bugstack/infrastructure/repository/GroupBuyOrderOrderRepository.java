package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.groupbuy.model.aggregate.GroupBuyOrderAggregate;
import cn.bugstack.domain.groupbuy.model.aggregate.GroupBuyTeamSettlementAggregate;
import cn.bugstack.domain.groupbuy.model.entity.*;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyOrderRepository;
import cn.bugstack.infrastructure.dao.IGroupBuyNotifyTaskDao;
import cn.bugstack.infrastructure.dao.IGroupBuyOrderDao;
import cn.bugstack.infrastructure.dao.IGroupBuyTeamDao;
import cn.bugstack.infrastructure.dao.po.groupbuy.GroupBuyNotifyTask;
import cn.bugstack.infrastructure.dao.po.groupbuy.GroupBuyOrder;
import cn.bugstack.infrastructure.dao.po.groupbuy.GroupBuyTeam;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Repository
public class GroupBuyOrderOrderRepository implements IGroupBuyOrderRepository {

    @Resource
    private IGroupBuyTeamDao groupBuyTeamDao;
    @Resource
    private IGroupBuyOrderDao groupBuyOrderDao;
    @Resource
    private IGroupBuyNotifyTaskDao groupBuyNotifyTaskDao;

    @Override
    public GroupBuyOrderEntity queryGroupBuyOrderByOutTradeNo(String userId, String outTradeNo) {
        GroupBuyOrder order = groupBuyOrderDao.queryGroupBuyOrderByOutTradeNo(userId, outTradeNo);
        return order == null ? null : toOrderEntity(order);
    }

    @Override
    public Integer countUserGroupBuyOrders(String userId, Long activityId) {
        return groupBuyOrderDao.queryOrderCountByUserIdAndActivityId(userId, activityId);
    }

    @Override
    @Transactional(timeout = 500)
    public GroupBuyOrderEntity lockGroupBuyOrder(GroupBuyOrderAggregate aggregate) {
        GroupBuyTrialResult trialResult = aggregate.getTrialResult();

        String teamId = aggregate.getTeamId();
        if (StringUtils.isBlank(teamId)) {
            teamId = RandomStringUtils.randomNumeric(8);
            LocalDateTime now = LocalDateTime.now();
            int validTime = trialResult.getValidTime() == null ? 0 : trialResult.getValidTime();

            groupBuyTeamDao.insert(GroupBuyTeam.builder()
                    .teamId(teamId)
                    .activityId(trialResult.getActivityId())
                    .initiatorUserId(aggregate.getUserId())
                    .targetCount(trialResult.getTargetCount())
                    .completeCount(0)
                    .lockCount(1)
                    .status(0)
                    .validStartTime(now)
                    .validEndTime(now.plusMinutes(validTime))
                    .notifyUrl(aggregate.getNotifyUrl())
                    .build());
        } else {
            int updated = groupBuyTeamDao.updateAddLockCount(teamId);
            if (updated != 1) {
                throw new AppException(ResponseCode.E0005);
            }
        }

        String orderId = RandomStringUtils.randomNumeric(12);
        GroupBuyOrderEntity orderEntity = GroupBuyOrderEntity.builder()
                .orderId(orderId)
                .userId(aggregate.getUserId())
                .teamId(teamId)
                .activityId(trialResult.getActivityId())
                .productId(trialResult.getProductId())
                .productName(trialResult.getProductName())
                .quantity(1)
                .source(aggregate.getSource())
                .channel(aggregate.getChannel())
                .originalAmount(trialResult.getOriginalPrice())
                .deductionAmount(trialResult.getDeductionPrice())
                .payAmount(trialResult.getPayPrice())
                .status(0)
                .outTradeNo(aggregate.getOutTradeNo())
                .build();

        groupBuyOrderDao.insert(toOrderPo(orderEntity));
        return orderEntity;
    }

    @Override
    @Transactional(timeout = 500)
    public GroupBuyNotifyTaskEntity settlementGroupBuyOrder(GroupBuyTeamSettlementAggregate aggregate) {
        GroupBuySettlementCommandEntity command = aggregate.getSettlementCommand();
        GroupBuyTeamEntity team = aggregate.getGroupBuyTeamEntity();

        // 首先-更新 “订单” 状态为已完成
        int orderUpdated = groupBuyOrderDao.updateOrderStatus2Complete(
                command.getUserId(),
                command.getOutTradeNo()
        );
        if (orderUpdated != 1) {
            throw new AppException(ResponseCode.E0005, "拼团订单结算更新失败");
        }

        // 其次-更新 “拼团” complete_count +1
        int completeUpdated = groupBuyTeamDao.updateAddCompleteCount(team.getTeamId());
        if (completeUpdated != 1) {
            throw new AppException(ResponseCode.E0005, "拼团团队完成人数更新失败");
        }

        // 未成团-返回空
        boolean complete = team.getTargetCount() - team.getCompleteCount() == 1;
        if (!complete) {
            return null;
        }
        // 成团
        int statusUpdated = groupBuyTeamDao.updateStatus2Complete(team.getTeamId());
        if (statusUpdated != 1) {
            throw new AppException(ResponseCode.E0005, "拼团团队状态更新失败");
        }

        // 查询已成团的所有订单号，组装通知任务
        List<String> outTradeNoList = groupBuyOrderDao.queryCompleteOutTradeNoListByTeamId(team.getTeamId());
        String parameterJson = JSON.toJSONString(new HashMap<String, Object>() {{
            put("teamId", team.getTeamId());
            put("outTradeNoList", outTradeNoList);
        }});

        // 创建回调任务
        groupBuyNotifyTaskDao.insert(GroupBuyNotifyTask.builder()
                .teamId(team.getTeamId())
                .activityId(team.getActivityId())
                .notifyMq("topic.team_success")
                .notifyStatus(0)     // 待发
                .notifyCount(0)
                .parameterJson(parameterJson)
                .build());

        // 返回领域对象
        return GroupBuyNotifyTaskEntity.builder()
                .teamId(team.getTeamId())
                .activityId(team.getActivityId())
                .notifyType("MQ")
                .notifyMQ("topic.team_success")
                .notifyCount(0)
                .notifyStatus(0)
                .parameterJson(parameterJson)
                .uuid(team.getTeamId() + "_trade_settlement_" + command.getOutTradeNo())
                .build();
    }

    private GroupBuyOrderEntity toOrderEntity(GroupBuyOrder order) {
        return GroupBuyOrderEntity.builder()
                .id(order.getId())
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .teamId(order.getTeamId())
                .activityId(order.getActivityId())
                .productId(order.getProductId())
                .productName(order.getProductName())
                .quantity(order.getQuantity())
                .source(order.getSource())
                .channel(order.getChannel())
                .originalAmount(order.getOriginalAmount())
                .deductionAmount(order.getDeductionAmount())
                .payAmount(order.getPayAmount())
                .status(order.getStatus())
                .outTradeNo(order.getOutTradeNo())
                .createTime(order.getCreateTime())
                .updateTime(order.getUpdateTime())
                .build();
    }

    private GroupBuyOrder toOrderPo(GroupBuyOrderEntity entity) {
        return GroupBuyOrder.builder()
                .orderId(entity.getOrderId())
                .userId(entity.getUserId())
                .teamId(entity.getTeamId())
                .activityId(entity.getActivityId())
                .productId(entity.getProductId())
                .productName(entity.getProductName())
                .quantity(entity.getQuantity())
                .source(entity.getSource())
                .channel(entity.getChannel())
                .originalAmount(entity.getOriginalAmount())
                .deductionAmount(entity.getDeductionAmount())
                .payAmount(entity.getPayAmount())
                .status(entity.getStatus())
                .outTradeNo(entity.getOutTradeNo())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
