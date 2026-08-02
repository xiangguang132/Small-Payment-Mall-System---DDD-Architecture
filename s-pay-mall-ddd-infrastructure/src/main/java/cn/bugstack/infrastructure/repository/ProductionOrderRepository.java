package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.production.model.aggregate.ProductionOrderAggregate;
import cn.bugstack.domain.production.model.vo.ProductionOrderMaterialVO;
import cn.bugstack.domain.production.repository.IProductionOrderRepository;
import cn.bugstack.infrastructure.dao.IProductionOrderDao;
import cn.bugstack.infrastructure.dao.IProductionOrderMaterialDao;
import cn.bugstack.infrastructure.dao.po.ProductionOrder;
import cn.bugstack.infrastructure.dao.po.ProductionOrderMaterial;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ProductionOrderRepository implements IProductionOrderRepository {

    @Resource
    private IProductionOrderDao productionOrderDao;
    @Resource
    private IProductionOrderMaterialDao productionOrderMaterialDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrder(ProductionOrderAggregate order) {
        if (order == null) {
            throw new IllegalArgumentException("生产单创建时不能为空");
        }

        ProductionOrder productionOrder = toProductionOrder(order);
        productionOrderDao.insert(productionOrder);
        return productionOrder.getId();
    }

    @Override
    public void saveOrderMaterials(Long orderId, List<ProductionOrderMaterialVO> materials) {
        if (orderId == null) {
            throw new IllegalArgumentException("生产单ID不能为空");
        }
        if (materials == null || materials.isEmpty()) {
            return;
        }

        List<ProductionOrderMaterial> materialList = materials.stream()
                .map(material -> toProductionOrderMaterial(orderId, material))
                .collect(Collectors.toList());
        productionOrderMaterialDao.insertBatch(materialList);
    }

    private ProductionOrder toProductionOrder(ProductionOrderAggregate order) {
        ProductionOrder productionOrder = new ProductionOrder();
        productionOrder.setId(order.getId());
        productionOrder.setOrderNo(order.getOrderNo());
        productionOrder.setProductId(order.getProductId());
        productionOrder.setProductQuantity(order.getProductQuantity());
        productionOrder.setWarehouseId(order.getWarehouseId());
        productionOrder.setStatus(order.getStatus());
        productionOrder.setIsDel(order.getIsDel());
        productionOrder.setCreateTime(order.getCreateTime());
        productionOrder.setUpdateTime(order.getUpdateTime());
        return productionOrder;
    }

    private ProductionOrderMaterial toProductionOrderMaterial(Long orderId, ProductionOrderMaterialVO material) {
        return ProductionOrderMaterial.builder()
                .id(material.getId())
                .productionOrderId(orderId)
                .materialId(material.getMaterialId())
                .materialQuantity(material.getMaterialQuantity())
                .allocationNo(material.getAllocationNo())
                .status(material.getStatus())
                .isDel(material.getIsDel())
                .createTime(material.getCreateTime())
                .updateTime(material.getUpdateTime())
                .build();
    }
}
