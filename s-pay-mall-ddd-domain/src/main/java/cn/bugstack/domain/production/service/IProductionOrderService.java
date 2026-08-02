package cn.bugstack.domain.production.service;

import cn.bugstack.domain.production.model.vo.ProductionOrderMaterialVO;

import java.util.List;

public interface IProductionOrderService {

    Long createOrder(Long productId, Integer productQuantity, Long warehouseId, List<ProductionOrderMaterialVO> materials);
}
