package cn.bugstack.domain.production.service;

import cn.bugstack.domain.production.model.aggregate.ProductionOrderAggregate;
import cn.bugstack.domain.production.model.vo.ProductionOrderMaterialVO;

import java.util.List;

public interface IProductionOrderService {

    Long createOrder(Long productId, String requestNo, Integer productQuantity, Long warehouseId, List<ProductionOrderMaterialVO> materials);

    ProductionOrderAggregate queryProductionOrderById(Long id);

    void executeCreatedOrders();

//    void executeOrder(Long productionOrderId);
}
