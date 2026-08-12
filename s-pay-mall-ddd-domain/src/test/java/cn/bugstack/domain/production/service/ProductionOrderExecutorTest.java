package cn.bugstack.domain.production.service;

import cn.bugstack.domain.materialstockallocation.service.IMaterialStockAllocationService;
import cn.bugstack.domain.production.exception.ProductionExecuteException;
import cn.bugstack.domain.production.model.aggregate.ProductionOrderAggregate;
import cn.bugstack.domain.production.model.vo.ProductionExecuteStageVO;
import cn.bugstack.domain.production.model.vo.ProductionOrderMaterialVO;
import cn.bugstack.domain.production.model.vo.ProductionOrderStatusVO;
import cn.bugstack.domain.production.repository.IProductionOrderRepository;
import cn.bugstack.domain.warehousestock.service.IStockService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProductionOrderExecutorTest {

    @Mock
    private IProductionOrderRepository productionOrderRepository;

    @Mock
    private IMaterialStockAllocationService materialStockAllocationService;

    @Mock
    private IStockService stockService;

    @InjectMocks
    private ProductionOrderExecutor executor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldThrowWhenOrderMissing() {
        when(productionOrderRepository.queryById(1L)).thenReturn(null);

        try {
            executor.execute(1L);
        } catch (ProductionExecuteException e) {
            assertEquals(ProductionExecuteStageVO.QUERY_ORDER, e.getStage());
            assertEquals("生产需求单不存在", e.getMessage());
            return;
        }
        throw new AssertionError("expected ProductionExecuteException");
    }

    @Test
    public void shouldDoNothingWhenOrderNotExecutable() {
        ProductionOrderAggregate order = order(ProductionOrderStatusVO.PROCESSING, material());
        when(productionOrderRepository.queryById(1L)).thenReturn(order);

        executor.execute(1L);

        verify(productionOrderRepository, never()).updateOrderStatus(eq(1L), any(Integer.class));
        verify(materialStockAllocationService, never()).create(any(Long.class), any(Integer.class), anyString());
    }

    @Test
    public void shouldExecuteCompleteFlow() {
        ProductionOrderAggregate order = order(ProductionOrderStatusVO.CREATED, material());
        when(productionOrderRepository.queryById(1L)).thenReturn(order);
        when(materialStockAllocationService.create(4L, 5, "生产单PO001备料")).thenReturn("MSA001");

        executor.execute(1L);

        verify(productionOrderRepository).updateOrderStatus(1L, ProductionOrderStatusVO.PROCESSING);
        verify(productionOrderRepository).updateMaterialAllocationNo(10L, "MSA001", 0);
        verify(productionOrderRepository).updateMaterialAllocationNo(10L, "MSA001", 1);
        verify(productionOrderRepository).updateMaterialAllocationNo(10L, "MSA001", 2);
        verify(stockService).inbound(3L, 2L, 2, "PRODUCTION_ORDER", "PO001");
        verify(productionOrderRepository).updateOrderStatus(1L, ProductionOrderStatusVO.COMPLETED);
    }

    @Test
    public void shouldWrapAllocationFailureWithStage() {
        ProductionOrderAggregate order = order(ProductionOrderStatusVO.CREATED, material());
        when(productionOrderRepository.queryById(1L)).thenReturn(order);
        when(materialStockAllocationService.create(4L, 5, "生产单PO001备料"))
                .thenThrow(new IllegalStateException("no stock"));

        try {
            executor.execute(1L);
        } catch (ProductionExecuteException e) {
            assertEquals(ProductionExecuteStageVO.CREATE_ALLOCATION, e.getStage());
            assertTrue(e.getMessage().contains("no stock"));
            return;
        }
        throw new AssertionError("expected ProductionExecuteException");
    }

    private ProductionOrderAggregate order(Integer status, ProductionOrderMaterialVO material) {
        return ProductionOrderAggregate.builder()
                .id(1L)
                .orderNo("PO001")
                .productId(2L)
                .productQuantity(2L)
                .warehouseId(3L)
                .status(status)
                .materials(Collections.singletonList(material))
                .build();
    }

    private ProductionOrderMaterialVO material() {
        return ProductionOrderMaterialVO.builder()
                .id(10L)
                .materialId(4L)
                .materialQuantity(5)
                .status(0)
                .build();
    }
}
