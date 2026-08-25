package cn.bugstack.domain.order.service;

import cn.bugstack.domain.order.model.entity.OrderLockEntity;

public interface IOrderLockService {

    /**
     * 锁单：构建聚合体 -> 持久化 -> 返回锁单实体
     * @return
     * @throws Exception
     */
    OrderLockEntity lockOrder();

    /**
     * 查询锁单状态
     * @param lockId
     * @return
     */
    OrderLockEntity queryLocalStatus(String lockId);

}
