package cn.bugstack.domain.production.repository;

import cn.bugstack.domain.production.model.aggregate.ProductionOrderAggregate;
import cn.bugstack.domain.production.model.vo.ProductionOrderMaterialVO;

import java.time.LocalDateTime;
import java.util.List;

public interface IProductionOrderRepository {

    Long saveOrder(ProductionOrderAggregate order);

    void saveOrderMaterials(Long orderId, List<ProductionOrderMaterialVO> materials);

    ProductionOrderAggregate queryById(Long id);

    boolean existsRecentSameOrder(Long productId,
                                  Long productQuantity,
                                  Long warehouseId,
                                  Integer status,
                                  Integer isDel,
                                  LocalDateTime startTime);

    List<ProductionOrderAggregate> queryCreatedOrders(Integer limit);

    void updateOrderStatus(Long orderId, Integer status);

    void updateMaterialAllocationNo(Long orderMaterialId, String allocationNo, Integer status);
}
