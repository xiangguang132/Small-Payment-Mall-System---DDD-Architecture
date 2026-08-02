package cn.bugstack.domain.production.repository;

import cn.bugstack.domain.production.model.aggregate.ProductionOrderAggregate;
import cn.bugstack.domain.production.model.vo.ProductionOrderMaterialVO;

import java.util.List;

public interface IProductionOrderRepository {

    Long saveOrder(ProductionOrderAggregate order);

    void saveOrderMaterials(Long orderId, List<ProductionOrderMaterialVO> materials);
}
