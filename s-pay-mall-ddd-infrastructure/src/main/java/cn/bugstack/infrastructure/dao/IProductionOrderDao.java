package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.product.ProductionOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface IProductionOrderDao {

    void insert(ProductionOrder order);

    ProductionOrder queryById(@Param("id") Long id);

    ProductionOrder queryByRequestNo(@Param("requestNo") String requestNo);

    List<ProductionOrder> queryList(@Param("status") Integer status,
                                    @Param("productId") Long productId,
                                    @Param("warehouseId") Long warehouseId,
                                    @Param("offset") Integer offset,
                                    @Param("pageSize") Integer pageSize);

    Long countList(@Param("status") Integer status,
                   @Param("productId") Long productId,
                   @Param("warehouseId") Long warehouseId);

    List<ProductionOrder> queryByStatus(@Param("status") Integer status, @Param("limit") Integer limit);

    List<ProductionOrder> queryExecutableOrders(@Param("limit") Integer limit);

    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    void recordExecuteFailure(@Param("id") Long id,
                              @Param("failReason") String failReason,
                              @Param("nextRetryTime") LocalDateTime nextRetryTime,
                              @Param("maxRetryCount") Integer maxRetryCount,
                              @Param("failStage") String failStage,
                              @Param("needManualIntervention") Integer needManualIntervention);
}
