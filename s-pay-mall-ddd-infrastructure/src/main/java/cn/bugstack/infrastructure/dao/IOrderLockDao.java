package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.payment.OrderLock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IOrderLockDao {

    void insert(OrderLock orderLock);

    OrderLock queryByLockId(@Param("lockId") String lockId);

    void updateOrderId(@Param("lockId") String lockId, @Param("orderId") String orderId);

    void updateLockStatus(@Param("lockId") String lockId, @Param("lockStatus") String lockStatus);

}
