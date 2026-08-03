package cn.bugstack.trigger.assembler;

import cn.bugstack.api.response.production.ProductionOrderDetailResponse;
import cn.bugstack.domain.production.model.aggregate.ProductionOrderAggregate;
import cn.bugstack.domain.production.model.vo.ProductionOrderMaterialVO;

import java.util.List;
import java.util.stream.Collectors;

public class ProductionOrderAssembler {

    private ProductionOrderAssembler() {
    }

    public static ProductionOrderDetailResponse toDetailResponse(ProductionOrderAggregate productionOrder) {
        return toDetailResponse(productionOrder, productionOrder == null ? null : productionOrder.getMaterials());
    }

    public static ProductionOrderDetailResponse toDetailResponse(ProductionOrderAggregate productionOrder, List<ProductionOrderMaterialVO> materials) {
        if (productionOrder == null) {
            throw new IllegalArgumentException("生产需求单不存在");
        }

        ProductionOrderDetailResponse response = new ProductionOrderDetailResponse();
        response.setId(productionOrder.getId());
        response.setOrderNo(productionOrder.getOrderNo());
        response.setRequestNo(productionOrder.getRequestNo());
        response.setProductId(productionOrder.getProductId());
        response.setProductQuantity(productionOrder.getProductQuantity());
        response.setWarehouseId(productionOrder.getWarehouseId());
        response.setRetryCount(productionOrder.getRetryCount());
        response.setFailReason(productionOrder.getFailReason());
        response.setNextRetryTime(productionOrder.getNextRetryTime());
        response.setStatus(productionOrder.getStatus());
        response.setIsDel(productionOrder.getIsDel());
        response.setCreateTime(productionOrder.getCreateTime());
        response.setUpdateTime(productionOrder.getUpdateTime());
        response.setMaterials(toMaterialItems(materials));
        return response;
    }

    private static List<ProductionOrderDetailResponse.MaterialItem> toMaterialItems(List<ProductionOrderMaterialVO> materials) {
        if (materials == null) {
            return null;
        }
        return materials.stream()
                .map(ProductionOrderAssembler::toMaterialItem)
                .collect(Collectors.toList());
    }

    private static ProductionOrderDetailResponse.MaterialItem toMaterialItem(ProductionOrderMaterialVO material) {
        if (material == null) {
            return null;
        }

        ProductionOrderDetailResponse.MaterialItem item = new ProductionOrderDetailResponse.MaterialItem();
        item.setId(material.getId());
        item.setProductionOrderId(material.getProductionOrderId());
        item.setMaterialId(material.getMaterialId());
        item.setMaterialQuantity(material.getMaterialQuantity());
        item.setAllocationNo(material.getAllocationNo());
        item.setStatus(material.getStatus());
        item.setIsDel(material.getIsDel());
        item.setCreateTime(material.getCreateTime());
        item.setUpdateTime(material.getUpdateTime());
        return item;
    }
}
