package cn.bugstack;

import cn.bugstack.domain.material.model.aggregate.MaterialAggregate;
import cn.bugstack.domain.material.service.IMaterialService;
import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;
import cn.bugstack.domain.materialstock.repository.IMaterialStockRepository;
import cn.bugstack.domain.materialstock.service.IMaterialStockService;
import cn.bugstack.domain.materialtype.model.aggregate.MaterialTypeAggregate;
import cn.bugstack.domain.materialtype.service.IMaterialTypeService;
import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import cn.bugstack.domain.product.service.IProductService;
import cn.bugstack.domain.production.model.aggregate.ProductionOrderAggregate;
import cn.bugstack.domain.production.model.vo.ProductionOrderMaterialVO;
import cn.bugstack.domain.production.model.vo.ProductionOrderStatusVO;
import cn.bugstack.domain.production.service.IProductionOrderService;
import cn.bugstack.domain.production.service.ProductionOrderExecutor;
import cn.bugstack.domain.producttype.model.aggregate.ProductTypeAggregate;
import cn.bugstack.domain.producttype.service.IProductTypeService;
import cn.bugstack.domain.warehouse.model.aggregate.WarehouseAggregate;
import cn.bugstack.domain.warehouse.service.IWarehouseService;
import cn.bugstack.domain.warehousestock.model.aggregate.StockAggregate;
import cn.bugstack.domain.warehousestock.service.IStockService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
@SpringBootTest(classes = Application.class, properties = {
        "production.order.job.enabled=false"
})
public class ProductionFlowE2ETest {

    @Resource
    private IMaterialTypeService materialTypeService;
    @Resource
    private IMaterialService materialService;
    @Resource
    private IMaterialStockService materialStockService;
    @Resource
    private IMaterialStockRepository materialStockRepository;
    @Resource
    private IProductTypeService productTypeService;
    @Resource
    private IProductService productService;
    @Resource
    private IWarehouseService warehouseService;
    @Resource
    private IProductionOrderService productionOrderService;
    @Resource
    private ProductionOrderExecutor productionOrderExecutor;
    @Resource
    private IStockService stockService;

    @Test
    public void shouldMoveMaterialStockToProductWarehouseStock() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String materialStorageAddress = "E2E-MATERIAL-" + suffix;

        Long materialTypeId = materialTypeService.addNewMaterialType(MaterialTypeAggregate.create(
                0L,
                "E2E原料分类" + suffix,
                "生产流程验收测试原料分类",
                "MT-E2E-" + suffix,
                0,
                1
        ));
        Long materialId = materialService.addNewMaterial(MaterialAggregate.create(
                "M-E2E-" + suffix,
                "E2E原料" + suffix,
                materialTypeId,
                "件",
                "生产流程验收测试原料",
                1
        ));
        materialStockService.inbound(
                materialId,
                materialStorageAddress,
                new BigDecimal("20"),
                "生产流程验收测试原料入库"
        );

        MaterialStockAggregate materialStockBefore = queryMaterialStock(materialId, materialStorageAddress);
        assertBigDecimalEquals("20", materialStockBefore.getAvailableQty());
        assertBigDecimalEquals("0", materialStockBefore.getLockedQty());
        assertBigDecimalEquals("20", materialStockBefore.getTotalQty());

        Long productTypeId = productTypeService.addNewProductType(ProductTypeAggregate.create(
                0L,
                "E2E商品分类" + suffix,
                "生产流程验收测试商品分类",
                "PT-E2E-" + suffix,
                0,
                1
        ));
        Long productId = productService.addNewProduct(ProductAggregate.create(
                "E2E成品" + suffix,
                "生产流程验收测试成品",
                "SKU-E2E-" + suffix,
                productTypeId,
                1,
                new BigDecimal("19.90"),
                null,
                null
        ));
        Long warehouseId = warehouseService.addWarehouse(WarehouseAggregate.create(
                "WH-E2E-" + suffix,
                "E2E成品仓" + suffix,
                1,
                "生产流程验收测试仓库",
                "E2E",
                "13800000000",
                1
        ));

        Long productionOrderId = productionOrderService.createOrder(
                productId,
                "REQ-E2E-" + suffix,
                3,
                warehouseId,
                Collections.singletonList(ProductionOrderMaterialVO.builder()
                        .materialId(materialId)
                        .materialQuantity(12)
                        .build())
        );

        productionOrderExecutor.execute(productionOrderId);

        ProductionOrderAggregate productionOrder = productionOrderService.queryProductionOrderById(productionOrderId);
        assertEquals(Integer.valueOf(ProductionOrderStatusVO.COMPLETED), productionOrder.getStatus());
        assertEquals(1, productionOrder.getMaterials().size());
        ProductionOrderMaterialVO orderMaterial = productionOrder.getMaterials().get(0);
        assertNotNull(orderMaterial.getAllocationNo());
        assertEquals(Integer.valueOf(2), orderMaterial.getStatus());

        MaterialStockAggregate materialStockAfter = queryMaterialStock(materialId, materialStorageAddress);
        assertBigDecimalEquals("8", materialStockAfter.getAvailableQty());
        assertBigDecimalEquals("0", materialStockAfter.getLockedQty());
        assertBigDecimalEquals("8", materialStockAfter.getTotalQty());

        StockAggregate productStock = stockService.queryStockByWarehouseIdAndProductId(warehouseId, productId);
        assertNotNull(productStock);
        assertBigDecimalEquals("3", productStock.getAvailableQty());
        assertBigDecimalEquals("0", productStock.getLockedQty());
        assertBigDecimalEquals("3", productStock.getTotalQty());
    }

    private MaterialStockAggregate queryMaterialStock(Long materialId, String storageAddress) {
        return materialStockRepository.queryAvailableByMaterialId(materialId)
                .stream()
                .filter(stock -> storageAddress.equals(stock.getStorageAddress()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("未找到验收测试原料库存记录"));
    }

    private void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertNotNull(actual);
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
