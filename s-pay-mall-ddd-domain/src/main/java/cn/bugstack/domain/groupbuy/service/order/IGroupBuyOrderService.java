package cn.bugstack.domain.groupbuy.service.order;

import cn.bugstack.domain.groupbuy.model.aggregate.GroupBuyOrderAggregate;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyOrderEntity;

import java.util.List;

public interface IGroupBuyOrderService {

    GroupBuyOrderEntity lockGroupBuyOrder(GroupBuyOrderAggregate aggregate);

    /**
     * 分页查询拼团订单
     * @param status 活动状态（null不过滤）
     * @param userId  用户ID（null/空不过滤）
     * @param pageNo  页码
     * @param pageSize 每页条数
     * @return 订单列表
     */
    List<GroupBuyOrderEntity> queryPageByStatusAndUserId(Integer status, String userId, Integer pageNo, Integer pageSize);

    /**
     * 统计拼团订单数量
     * @param status 活动状态（null不过滤）
     * @param userId  用户ID（null/空不过滤）
     * @return 总数
     */
    long countByStatusAndUserId(Integer status, String userId);
}
