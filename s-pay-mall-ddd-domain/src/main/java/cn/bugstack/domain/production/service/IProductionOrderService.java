package cn.bugstack.domain.production.service;

import cn.bugstack.domain.production.model.aggregate.ProductionOrderAggregate;
import cn.bugstack.domain.production.model.vo.ProductionOrderMaterialVO;

import java.util.List;

public interface IProductionOrderService {

    Long createOrder(Long productId, String requestNo, Integer productQuantity, Long warehouseId, List<ProductionOrderMaterialVO> materials);

    ProductionOrderAggregate queryProductionOrderById(Long id);

    List<ProductionOrderAggregate> queryProductionOrders(Integer status, Long productId, Long warehouseId, Integer pageNo, Integer pageSize);

    Long countProductionOrders(Integer status, Long productId, Long warehouseId);

    void executeCreatedOrders();

    void handExecuteById(Long id);

    void handRetryById(Long id);

    void cancelProductionOrderById(Long id);

}
