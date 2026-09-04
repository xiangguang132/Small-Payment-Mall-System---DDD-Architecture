package cn.bugstack.domain.order.service;

import cn.bugstack.domain.order.model.entity.OrderLockEntity;

import java.util.List;

public interface IOrderLockService {

    /**
     * 锁单：构建聚合体 -> 持久化 -> 返回锁单实体
     * @param couponIds 优惠券ID列表（可为null）
     */
    OrderLockEntity lockOrder(List<String> couponIds);

    /**
     * 查询锁单状态
     * @param lockId
     * @return
     */
    OrderLockEntity queryLocalStatus(String lockId);

}
