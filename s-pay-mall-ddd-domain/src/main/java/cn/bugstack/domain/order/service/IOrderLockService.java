package cn.bugstack.domain.order.service;

import cn.bugstack.domain.order.model.entity.OrderLockEntity;

public interface IOrderLockService {

    /**
     * 锁单：校验商品 -> 构建聚合体 -> 持久化 -> 返回锁单实体
     * @param userId
     * @param productId
     * @return
     * @throws Exception
     */
    OrderLockEntity lockOrder(String userId, String productId);

    /**
     * 查询锁单状态
     * @param lockId
     * @return
     */
    OrderLockEntity queryLocalStatus(String lockId);

}
