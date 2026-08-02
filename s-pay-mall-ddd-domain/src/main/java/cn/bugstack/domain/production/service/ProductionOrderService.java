package cn.bugstack.domain.production.service;

import cn.bugstack.domain.material.service.IMaterialService;
import cn.bugstack.domain.materialstockallocation.service.IMaterialStockAllocationService;
import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import cn.bugstack.domain.product.service.IProductService;
import cn.bugstack.domain.production.model.aggregate.ProductionOrderAggregate;
import cn.bugstack.domain.production.model.vo.ProductionOrderMaterialVO;
import cn.bugstack.domain.production.repository.IProductionOrderRepository;
import cn.bugstack.domain.warehouse.model.aggregate.WarehouseAggregate;
import cn.bugstack.domain.warehouse.service.IWarehouseService;
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

    @Resource
    private IProductService productService;

    @Resource
    private IWarehouseService warehouseService;

    @Resource
    private IMaterialService materialService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(Long productId, Integer productQuantity, Long warehouseId, List<ProductionOrderMaterialVO> materials) {
        validateCreateParams(productId, productQuantity, warehouseId, materials);
        validateRecentDuplicateOrder(
                productId,
                productQuantity.longValue(),
                warehouseId,
                STATUS_CREATED,
                0
        );
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductionOrderAggregate queryProductionOrderById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("生产需求单id不能为空");
        }
        return productionOrderRepository.queryById(id);
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

        validateProductEnabled(productId);
        validateWarehouseEnabled(warehouseId);

        for (ProductionOrderMaterialVO material : materials) {
            if (material.getMaterialId() == null) {
                throw new IllegalArgumentException("原料ID不能为空");
            }
            if (material.getMaterialQuantity() == null || material.getMaterialQuantity() <= 0) {
                throw new IllegalArgumentException("原料数量必须大于0");
            }

            materialService.validateMaterialEnabled(material.getMaterialId());
        }
    }

    private void validateProductEnabled(Long productId) {
        ProductAggregate product = productService.queryProductById(productId);
        if (product == null) {
            throw new IllegalArgumentException("生产商品不存在");
        }
        if (product.getIsDel() != null && product.getIsDel() == 1) {
            throw new IllegalArgumentException("生产商品已删除，不能使用");
        }
        if (product.getStatus() == null || product.getStatus() != 1) {
            throw new IllegalArgumentException("生产商品未上架，不能创建生产需求单");
        }
    }

    private void validateWarehouseEnabled(Long warehouseId) {
        WarehouseAggregate warehouse = warehouseService.queryWarehouseById(warehouseId);
        if (warehouse == null) {
            throw new IllegalArgumentException("入库仓库不存在");
        }
        if (warehouse.getIsDel() != null && warehouse.getIsDel() == 1) {
            throw new IllegalArgumentException("入库仓库已删除，不能使用");
        }
        if (warehouse.getStatus() == null || warehouse.getStatus() != 1) {
            throw new IllegalArgumentException("入库仓库未启用，不能创建生产需求单");
        }
    }

    private void validateRecentDuplicateOrder(Long productId,
                                              Long productQuantity,
                                              Long warehouseId,
                                              Integer status,
                                              Integer isDel) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);

        boolean exists = productionOrderRepository.existsRecentSameOrder(
                productId,
                productQuantity,
                warehouseId,
                status,
                isDel,
                startTime
        );

        if (exists) {
            throw new IllegalArgumentException("1小时内已存在相同生产需求单，请勿重复创建");
        }
    }

    private String generateOrderNo() {
        return "PO" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                ThreadLocalRandom.current().nextInt(1000, 10000);
    }
}
