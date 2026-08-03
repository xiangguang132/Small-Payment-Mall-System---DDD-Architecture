package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.ProductionOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface IProductionOrderDao {

    void insert(ProductionOrder order);

    ProductionOrder queryById(@Param("id") Long id);

    ProductionOrder queryByRequestNo(@Param("requestNo") String requestNo);

    List<ProductionOrder> queryByStatus(@Param("status") Integer status, @Param("limit") Integer limit);

    List<ProductionOrder> queryExecutableOrders(@Param("limit") Integer limit);

    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    void recordExecuteFailure(@Param("id") Long id,
                              @Param("failReason") String failReason,
                              @Param("nextRetryTime") LocalDateTime nextRetryTime,
                              @Param("maxRetryCount") Integer maxRetryCount);
}
