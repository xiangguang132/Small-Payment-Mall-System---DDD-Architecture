package cn.bugstack.domain.order.adapter.repository;

import cn.bugstack.domain.order.model.entity.OrderLockEntity;

public interface IOrderLockRepository {

    /** 保存锁单记录 */
    void saveLock(OrderLockEntity lockEntity);

    /** 根据 lockId 查询 */
    OrderLockEntity queryLockByLockId(String lockId);

    /** 更新锁单状态 */
    void updateLockStatus(String lockId, String status);

    /** 查询用户当前有效锁单（防重复锁） */
    OrderLockEntity queryLockedByUserProduct(String userId, String productId);

}
