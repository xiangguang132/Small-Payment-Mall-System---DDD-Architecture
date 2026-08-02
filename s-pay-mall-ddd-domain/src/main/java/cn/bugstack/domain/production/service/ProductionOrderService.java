package cn.bugstack.domain.production.service;

import cn.bugstack.domain.materialstockallocation.service.IMaterialStockAllocationService;
import cn.bugstack.domain.production.model.aggregate.ProductionOrderAggregate;
import cn.bugstack.domain.production.model.vo.ProductionOrderMaterialVO;
import cn.bugstack.domain.production.repository.IProductionOrderRepository;
import cn.bugstack.domain.warehousestock.service.IStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class ProductionOrderService implements IProductionOrderService {

    private static final int STATUS_CREATED = 0;
    private static final int STATUS_PROCESSING = 1;
    private static final int STATUS_COMPLETED = 2;
    private static final int STATUS_FAILED = 3;

    private static final int MATERIAL_STATUS_CREATED = 0;
    private static final int MATERIAL_STATUS_LOCKED = 1;

    @Resource
    private IProductionOrderRepository productionOrderRepository;

    @Resource
    private IMaterialStockAllocationService materialStockAllocationService;

    @Resource
    private IStockService stockService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(Long productId, Integer productQuantity, Long warehouseId, List<ProductionOrderMaterialVO> materials) {
        validateCreateParams(productId, productQuantity, warehouseId, materials);
        LocalDateTime now = LocalDateTime.now();
        ProductionOrderAggregate order = ProductionOrderAggregate.builder()
                .orderNo(generateOrderNo())
                .productId(productId)
                .productQuantity(productQuantity.longValue())
                .warehouseId(warehouseId)
                .status(STATUS_CREATED)
                .isDel(0)
                .createTime(now)
                .updateTime(now)
                .build();

        Long orderId = productionOrderRepository.saveOrder(order);

        for (ProductionOrderMaterialVO material : materials) {
            material.setProductionOrderId(orderId);
            material.setStatus(MATERIAL_STATUS_CREATED);
            material.setIsDel(0);
            material.setCreateTime(now);
            material.setUpdateTime(now);
        }

        productionOrderRepository.saveOrderMaterials(orderId, materials);
        return orderId;
    }

    private void validateCreateParams(Long productId, Integer productQuantity, Long warehouseId, List<ProductionOrderMaterialVO> materials) {
        if (productId == null) {
            throw new IllegalArgumentException("生产商品ID不能为空");
        }
        if (productQuantity == null || productQuantity <= 0) {
            throw new IllegalArgumentException("生产数量必须大于0");
        }
        if (warehouseId == null) {
            throw new IllegalArgumentException("入库仓库ID不能为空");
        }
        if (materials == null || materials.isEmpty()) {
            throw new IllegalArgumentException("生产原料不能为空");
        }

        for (ProductionOrderMaterialVO material : materials) {
            if (material.getMaterialId() == null) {
                throw new IllegalArgumentException("原料ID不能为空");
            }
            if (material.getMaterialQuantity() == null || material.getMaterialQuantity() <= 0) {
                throw new IllegalArgumentException("原料数量必须大于0");
            }
        }
    }

    private String generateOrderNo() {
        return "PO" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                ThreadLocalRandom.current().nextInt(1000, 10000);
    }
}
