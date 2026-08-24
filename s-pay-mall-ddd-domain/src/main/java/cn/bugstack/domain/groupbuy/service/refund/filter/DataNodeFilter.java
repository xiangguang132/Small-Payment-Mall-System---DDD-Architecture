package cn.bugstack.domain.groupbuy.service.refund.filter;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyOrderEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderBehaviorEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderCommandEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTeamEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyActivityRepository;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyOrderRepository;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyTeamRepository;
import cn.bugstack.domain.groupbuy.service.refund.factory.GroupBuyRefundOrderRuleFilterFactory;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import cn.bugstack.types.design.framework.link.multilink.handler.ILogicHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 数据----数据节点过滤器
 * 负责查询外部交易单和拼团状态数据加载
 */
@Slf4j
@Service
public class DataNodeFilter implements ILogicHandler<GroupBuyRefundOrderCommandEntity, GroupBuyRefundOrderRuleFilterFactory.DynamicContext, GroupBuyRefundOrderBehaviorEntity> {

    @Resource
    private IGroupBuyOrderRepository groupBuyOrderRepository;
    @Resource
    private IGroupBuyTeamRepository groupBuyTeamRepository;
    @Resource
    private IGroupBuyActivityRepository groupBuyActivityRepository;

    @Override
    public GroupBuyRefundOrderBehaviorEntity apply(GroupBuyRefundOrderCommandEntity requestParameter, GroupBuyRefundOrderRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("逆向流程-退单操作，数据加载节点 userId:{} outTradeNo:{}", requestParameter.getUserId(), requestParameter.getOutTradeNo());

        // 查询外部交易单子
        GroupBuyOrderEntity groupBuyOrderEntity = groupBuyOrderRepository.queryGroupBuyOrderByOutTradeNo(requestParameter.getUserId(), requestParameter.getOutTradeNo());
        if (groupBuyOrderEntity == null) {
            throw new AppException(ResponseCode.E0104);
        }

        String teamId = groupBuyOrderEntity.getTeamId();
        if (teamId == null || teamId.isEmpty()) {
            throw new AppException(ResponseCode.E0104, "退单失败，订单未关联拼团信息");
        }

        // 查询拼团
        GroupBuyTeamEntity groupBuyTeamEntity = groupBuyTeamRepository.queryGroupBuyTeamByTeamId(teamId);
        if (groupBuyTeamEntity == null) {
            throw new AppException(ResponseCode.E0104, "退单失败，未找到对应拼团信息");
        }

        // 查询活动
        Long activityId = groupBuyTeamEntity.getActivityId();
        GroupBuyActivityEntity groupBuyActivityEntity = groupBuyActivityRepository.queryGroupBuyActivityByActivityId(activityId);
        if (groupBuyActivityEntity == null) {
            throw new AppException(ResponseCode.E0104, "退单失败，未找到对应活动信息");
        }

        // 写入上下文
        dynamicContext.setGroupBuyOrderEntity(groupBuyOrderEntity);
        dynamicContext.setGroupBuyTeamEntity(groupBuyTeamEntity);
        dynamicContext.setGroupBuyActivityEntity(groupBuyActivityEntity);
        dynamicContext.setMessage("数据加载成功");

        // 当前链路由 BusinessLinkedList 统一推进，节点返回 null 表示继续下一个节点
        return null;
    }
}
