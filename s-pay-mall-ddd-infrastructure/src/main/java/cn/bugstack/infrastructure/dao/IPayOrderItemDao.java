package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.payment.PayOrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IPayOrderItemDao {

    /** 批量插入明细 */
    void batchInsert(@Param("items") List<PayOrderItem> items);

    /** 按订单号查询明细 */
    List<PayOrderItem> queryByOrderId(@Param("orderId") String orderId);

}