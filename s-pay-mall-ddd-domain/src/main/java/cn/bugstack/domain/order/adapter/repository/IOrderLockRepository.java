package cn.bugstack.domain.order.adapter.repository;

import cn.bugstack.domain.order.model.entity.OrderLockEntity;

public interface IOrderLockRepository {

    /** 保存锁单记录 */
    void saveLock(OrderLockEntity lockEntity);

    /** 根据 lockId 查询 */
    OrderLockEntity queryLockByLockId(String lockId);

    /** 确认锁单：回写 orderId 并更新状态为 CONFIRMED */
    void updateOrderId(String lockId, String orderId);

    /** 更新锁单状态（如过期 EXPIRED） */
    void updateLockStatus(String lockId, String status);

}
