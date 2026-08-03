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
import java.time.LocalDateTime;
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

    @Override
    public ProductionOrderAggregate queryById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("生产需求单id不能为空");
        }
        ProductionOrder productionOrder = productionOrderDao.queryById(id);
        if (productionOrder == null) return null;

        List<ProductionOrderMaterialVO> materials = productionOrderMaterialDao.queryByProductionOrderId(id).stream()
                .map(this::toProductionOrderMaterialVO)
                .collect(Collectors.toList());

        return toProductionOrderAggregate(productionOrder, materials);
    }

    @Override
    public ProductionOrderAggregate queryByRequestNo(String requestNo) {
        if (requestNo == null || requestNo.trim().isEmpty()) {
            throw new IllegalArgumentException("请求号不能为空");
        }

        ProductionOrder productionOrder = productionOrderDao.queryByRequestNo(requestNo.trim());
        if (productionOrder == null) return null;

        List<ProductionOrderMaterialVO> materials = productionOrderMaterialDao.queryByProductionOrderId(productionOrder.getId()).stream()
                .map(this::toProductionOrderMaterialVO)
                .collect(Collectors.toList());

        return toProductionOrderAggregate(productionOrder, materials);
    }

    @Override
    public List<ProductionOrderAggregate> queryCreatedOrders(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        return productionOrderDao.queryExecutableOrders(limit).stream()
                .map(productionOrder -> {
                    List<ProductionOrderMaterialVO> materials = productionOrderMaterialDao
                            .queryByProductionOrderId(productionOrder.getId())
                            .stream()
                            .map(this::toProductionOrderMaterialVO)
                            .collect(Collectors.toList());
                    return toProductionOrderAggregate(productionOrder, materials);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void updateOrderStatus(Long orderId, Integer status) {
        if (orderId == null) {
            throw new IllegalArgumentException("生产需求单ID不能为空");
        }
        if (status == null) {
            throw new IllegalArgumentException("生产需求单状态不能为空");
        }

        productionOrderDao.updateStatus(orderId, status);
    }

    @Override
    public void recordExecuteFailure(Long orderId,
                                     String failReason,
                                     LocalDateTime nextRetryTime,
                                     Integer maxRetryCount,
                                     String failStage,
                                     Integer needManualIntervention) {
        if (orderId == null) {
            throw new IllegalArgumentException("生产需求单ID不能为空");
        }
        if (maxRetryCount == null || maxRetryCount <= 0) {
            throw new IllegalArgumentException("最大重试次数必须大于0");
        }
        if (needManualIntervention == null) {
            needManualIntervention = 0;
        }

        productionOrderDao.recordExecuteFailure(orderId,
                trimFailReason(failReason),
                nextRetryTime,
                maxRetryCount,
                failStage,
                needManualIntervention);
    }

    @Override
    public void updateMaterialAllocationNo(Long orderMaterialId, String allocationNo, Integer status) {
        if (orderMaterialId == null) {
            throw new IllegalArgumentException("生产需求单原料明细ID不能为空");
        }
        if (allocationNo == null || allocationNo.trim().isEmpty()) {
            throw new IllegalArgumentException("原料备料单号不能为空");
        }
        if (status == null) {
            throw new IllegalArgumentException("生产需求单原料状态不能为空");
        }

        productionOrderMaterialDao.updateAllocationNo(orderMaterialId, allocationNo, status);
    }

    private ProductionOrderAggregate toProductionOrderAggregate(ProductionOrder productionOrder,
                                                                List<ProductionOrderMaterialVO> materials) {
        return ProductionOrderAggregate.builder()
                .id(productionOrder.getId())
                .orderNo(productionOrder.getOrderNo())
                .requestNo(productionOrder.getRequestNo())
                .productId(productionOrder.getProductId())
                .productQuantity(productionOrder.getProductQuantity())
                .warehouseId(productionOrder.getWarehouseId())
                .retryCount(productionOrder.getRetryCount())
                .failReason(productionOrder.getFailReason())
                .nextRetryTime(productionOrder.getNextRetryTime())
                .status(productionOrder.getStatus())
                .failStage(productionOrder.getFailStage())
                .needManualIntervention(productionOrder.getNeedManualIntervention())
                .isDel(productionOrder.getIsDel())
                .createTime(productionOrder.getCreateTime())
                .updateTime(productionOrder.getUpdateTime())
                .materials(materials)
                .build();
    }

    private ProductionOrder toProductionOrder(ProductionOrderAggregate order) {
        ProductionOrder productionOrder = new ProductionOrder();
        productionOrder.setId(order.getId());
        productionOrder.setOrderNo(order.getOrderNo());
        productionOrder.setRequestNo(order.getRequestNo());
        productionOrder.setProductId(order.getProductId());
        productionOrder.setProductQuantity(order.getProductQuantity());
        productionOrder.setWarehouseId(order.getWarehouseId());
        productionOrder.setRetryCount(order.getRetryCount());
        productionOrder.setFailReason(order.getFailReason());
        productionOrder.setNextRetryTime(order.getNextRetryTime());
        productionOrder.setStatus(order.getStatus());
        productionOrder.setFailStage(order.getFailStage());
        productionOrder.setNeedManualIntervention(order.getNeedManualIntervention());
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

    private ProductionOrderMaterialVO toProductionOrderMaterialVO(ProductionOrderMaterial material) {
        return ProductionOrderMaterialVO.builder()
                .id(material.getId())
                .productionOrderId(material.getProductionOrderId())
                .materialId(material.getMaterialId())
                .materialQuantity(material.getMaterialQuantity())
                .allocationNo(material.getAllocationNo())
                .status(material.getStatus())
                .isDel(material.getIsDel())
                .createTime(material.getCreateTime())
                .updateTime(material.getUpdateTime())
                .build();
    }

    private String trimFailReason(String failReason) {
        if (failReason == null) {
            return null;
        }
        return failReason.length() > 512 ? failReason.substring(0, 512) : failReason;
    }
}
