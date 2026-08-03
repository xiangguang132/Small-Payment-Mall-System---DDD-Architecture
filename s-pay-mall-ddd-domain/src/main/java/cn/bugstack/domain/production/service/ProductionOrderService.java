package cn.bugstack.domain.production.service;

import cn.bugstack.domain.material.service.IMaterialService;
import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import cn.bugstack.domain.product.service.IProductService;
import cn.bugstack.domain.production.model.aggregate.ProductionOrderAggregate;
import cn.bugstack.domain.production.model.vo.ProductionOrderMaterialVO;
import cn.bugstack.domain.production.model.vo.ProductionOrderStatusVO;
import cn.bugstack.domain.production.repository.IProductionOrderRepository;
import cn.bugstack.domain.warehouse.model.aggregate.WarehouseAggregate;
import cn.bugstack.domain.warehouse.service.IWarehouseService;
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

    private static final int MATERIAL_STATUS_CREATED = 0;
    private static final int MAX_EXECUTE_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_MINUTES = 1L;

    @Resource
    private IProductionOrderRepository productionOrderRepository;

    @Resource
    private IProductService productService;

    @Resource
    private IWarehouseService warehouseService;

    @Resource
    private IMaterialService materialService;

    @Resource
    private ProductionOrderExecutor productionOrderExecutor;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(Long productId, Integer productQuantity, Long warehouseId, List<ProductionOrderMaterialVO> materials) {
        validateCreateParams(productId, productQuantity, warehouseId, materials);
        validateRecentDuplicateOrder(
                productId,
                productQuantity.longValue(),
                warehouseId,
                ProductionOrderStatusVO.CREATED,
                0
        );
        LocalDateTime now = LocalDateTime.now();
        ProductionOrderAggregate order = ProductionOrderAggregate.builder()
                .orderNo(generateOrderNo())
                .productId(productId)
                .productQuantity(productQuantity.longValue())
                .warehouseId(warehouseId)
                .retryCount(0)
                .failReason(null)
                .nextRetryTime(null)
                .status(ProductionOrderStatusVO.CREATED)
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


    @Override
    public void executeCreatedOrders() {
        List<ProductionOrderAggregate> orders = productionOrderRepository.queryCreatedOrders(10);
        for (ProductionOrderAggregate order : orders) {
            try {
                productionOrderExecutor.execute(order.getId());
            } catch (Exception e) {
                log.warn("生产需求单执行失败 orderId:{} reason:{}", order.getId(), e.getMessage(), e);
                // 初始写法 - 失败之后直接修改订单状态为 单纯的失败
                // productionOrderRepository.updateOrderStatus(order.getId(), ProductionOrderStatusVO.RETRYABLE_FAILED);

                // 失败重试写法 - 失败之后 尝试在xx时间后重新尝试执行，此时定义状态为“失败可重试-3”，xx次后宣布“彻底失败-4”
                recordExecuteFailure(order, e);
            }
        }
    }

    /**
     * 验证创建请求合法性
     * @param productId
     * @param productQuantity
     * @param warehouseId
     * @param materials
     */
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

    /**
     * 验证商品合法性
     * @param productId
     */
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

    /**
     * 验证商品仓库合法性
     * @param warehouseId
     */
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

    /**
     * 验证 一小时内 是否存在重复生产需求单
     * @param productId
     * @param productQuantity
     * @param warehouseId
     * @param status
     * @param isDel
     */
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

    /**
     * 订单编号生成方法
     * @return
     */
    private String generateOrderNo() {
        return "PO" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    private void recordExecuteFailure(ProductionOrderAggregate order, Exception e) {
        if (order == null || order.getId() == null) {
            return;
        }

        int currentRetryCount = order.getRetryCount() == null ? 0 : order.getRetryCount();
        LocalDateTime nextRetryTime = currentRetryCount + 1 >= MAX_EXECUTE_RETRY_COUNT
                ? null
                : LocalDateTime.now().plusMinutes(RETRY_DELAY_MINUTES);
        String failReason = e == null ? "生产需求单执行失败" : e.getMessage();

        productionOrderRepository.recordExecuteFailure(
                order.getId(),
                failReason,
                nextRetryTime,
                MAX_EXECUTE_RETRY_COUNT
        );
    }
}
