package cn.bugstack.domain.production.service;

import cn.bugstack.domain.material.service.IMaterialService;
import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import cn.bugstack.domain.product.service.IProductService;
import cn.bugstack.domain.production.exception.ProductionExecuteException;
import cn.bugstack.domain.production.model.aggregate.ProductionOrderAggregate;
import cn.bugstack.domain.production.model.vo.ProductionExecuteStageVO;
import cn.bugstack.domain.production.model.vo.ProductionOrderMaterialVO;
import cn.bugstack.domain.production.model.vo.ProductionOrderStatusVO;
import cn.bugstack.domain.production.repository.IProductionOrderRepository;
import cn.bugstack.domain.warehouse.model.aggregate.WarehouseAggregate;
import cn.bugstack.domain.warehouse.service.IWarehouseService;
import cn.bugstack.types.exception.AppException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProductionOrderServiceTest {

    @Mock
    private IProductionOrderRepository productionOrderRepository;

    @Mock
    private IProductService productService;

    @Mock
    private IWarehouseService warehouseService;

    @Mock
    private IMaterialService materialService;

    @Mock
    private ProductionOrderExecutor productionOrderExecutor;

    @InjectMocks
    private ProductionOrderService productionOrderService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(productionOrderService, "maxExecuteRetryCount", 3);
        ReflectionTestUtils.setField(productionOrderService, "retryDelayMinutes", 5L);
    }

    @Test
    public void shouldCreateOrderAndInitializeMaterials() {
        when(productionOrderRepository.queryByRequestNo("REQ-001")).thenReturn(null);
        when(productService.queryProductById(1L)).thenReturn(enabledProduct());
        when(warehouseService.queryWarehouseById(2L)).thenReturn(enabledWarehouse());
        when(productionOrderRepository.saveOrder(any(ProductionOrderAggregate.class))).thenReturn(10L);
        ProductionOrderMaterialVO material = ProductionOrderMaterialVO.builder().materialId(3L).materialQuantity(2).build();

        Long id = productionOrderService.createOrder(1L, " REQ-001 ", 5, 2L, Collections.singletonList(material));

        assertEquals(Long.valueOf(10L), id);
        assertEquals(Integer.valueOf(0), material.getStatus());
        assertEquals(Integer.valueOf(0), material.getIsDel());
        assertNotNull(material.getCreateTime());
        verify(productionOrderRepository).saveOrderMaterials(10L, Collections.singletonList(material));
    }

    @Test(expected = AppException.class)
    public void shouldRejectDuplicateRequestNo() {
        when(productionOrderRepository.queryByRequestNo("REQ-001"))
                .thenReturn(ProductionOrderAggregate.builder().id(1L).build());

        productionOrderService.createOrder(1L, "REQ-001", 5, 2L, Collections.singletonList(material()));
    }

    @Test(expected = AppException.class)
    public void shouldRejectCreateWhenProductOffline() {
        when(productionOrderRepository.queryByRequestNo("REQ-001")).thenReturn(null);
        ProductAggregate product = enabledProduct();
        product.setStatus(0);
        when(productService.queryProductById(1L)).thenReturn(product);

        productionOrderService.createOrder(1L, "REQ-001", 5, 2L, Collections.singletonList(material()));
    }

    @Test(expected = AppException.class)
    public void shouldRejectManualRetryWhenStatusIsNotRetryable() {
        when(productionOrderRepository.queryById(1L))
                .thenReturn(ProductionOrderAggregate.builder().id(1L).status(ProductionOrderStatusVO.CREATED).build());

        productionOrderService.handRetryById(1L);
    }

    @Test
    public void shouldRecordFailureForManualInterventionStage() {
        ProductionOrderAggregate order = ProductionOrderAggregate.builder()
                .id(1L)
                .orderNo("PO001")
                .status(ProductionOrderStatusVO.CREATED)
                .retryCount(0)
                .build();
        when(productionOrderRepository.queryCreatedOrders(10)).thenReturn(Collections.singletonList(order));
        doThrow(new ProductionExecuteException(
                ProductionExecuteStageVO.OUTBOUND_MATERIAL,
                "material outbound failed"
        )).when(productionOrderExecutor).execute(1L);

        productionOrderService.executeCreatedOrders();

        verify(productionOrderRepository).recordExecuteFailure(
                eq(1L),
                eq("material outbound failed"),
                isNull(),
                eq(3),
                eq("OUTBOUND_MATERIAL"),
                eq(1)
        );
    }

    private ProductionOrderMaterialVO material() {
        return ProductionOrderMaterialVO.builder().materialId(3L).materialQuantity(2).build();
    }

    private ProductAggregate enabledProduct() {
        return ProductAggregate.builder().id(1L).status(1).isDel(0).build();
    }

    private WarehouseAggregate enabledWarehouse() {
        return WarehouseAggregate.builder().id(2L).status(1).isDel(0).build();
    }
}
