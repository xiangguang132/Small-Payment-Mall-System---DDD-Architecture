package cn.bugstack.domain.materialstock.service;

import cn.bugstack.domain.material.service.IMaterialService;
import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;
import cn.bugstack.domain.materialstock.repository.IMaterialStockRepository;
import cn.bugstack.types.exception.AppException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MaterialStockServiceTest {

    @Mock
    private IMaterialStockRepository materialStockRepository;

    @Mock
    private IMaterialService materialService;

    @InjectMocks
    private MaterialStockService materialStockService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = AppException.class)
    public void shouldRejectQueryWhenStockMissing() {
        when(materialStockRepository.queryById(1L)).thenReturn(null);

        materialStockService.queryMaterialStockById(1L);
    }

    @Test
    public void shouldInboundMaterialStock() {
        materialStockService.inbound(1L, " A-01 ", new BigDecimal("10.00"), "采购入库");

        verify(materialService).validateMaterialEnabled(1L);
        verify(materialStockRepository).inbound(1L, "A-01", new BigDecimal("10.00"));
    }

    @Test
    public void shouldAdjustStockAndKeepAvailableQty() {
        MaterialStockAggregate stock = MaterialStockAggregate.builder()
                .id(1L)
                .materialId(1L)
                .storageAddress("A-01")
                .totalQty(new BigDecimal("10.00"))
                .lockedQty(new BigDecimal("5.00"))
                .availableQty(new BigDecimal("5.00"))
                .build();
        when(materialStockRepository.queryById(1L)).thenReturn(stock);

        MaterialStockAggregate adjusted = materialStockService.adjust(
                1L,
                2L,
                " B-02 ",
                new BigDecimal("15.00"),
                "盘点"
        );

        assertEquals(Long.valueOf(2L), adjusted.getMaterialId());
        assertEquals("B-02", adjusted.getStorageAddress());
        assertEquals(new BigDecimal("15.00"), adjusted.getTotalQty());
        assertEquals(new BigDecimal("10.00"), adjusted.getAvailableQty());
        verify(materialStockRepository).updateById(stock);
    }

    @Test(expected = AppException.class)
    public void shouldRejectManualOutboundWhenAvailableStockNotEnough() {
        when(materialStockRepository.queryById(1L)).thenReturn(stock());
        when(materialStockRepository.outboundAvailableStock(eq(1L), any(BigDecimal.class))).thenReturn(false);

        materialStockService.manualOutbound(1L, 5, "领料");
    }

    @Test
    public void shouldManualOutboundWhenStockIsEnough() {
        when(materialStockRepository.queryById(1L)).thenReturn(stock());
        when(materialStockRepository.outboundAvailableStock(eq(1L), any(BigDecimal.class))).thenReturn(true);

        MaterialStockAggregate result = materialStockService.manualOutbound(1L, 3, "领料");

        assertNotNull(result);
        verify(materialStockRepository).outboundAvailableStock(eq(1L), any(BigDecimal.class));
    }

    @Test
    public void shouldLockAndReleaseStock() {
        when(materialStockRepository.queryById(1L)).thenReturn(stock());
        when(materialStockRepository.lockStock(eq(1L), any(BigDecimal.class))).thenReturn(true);
        when(materialStockRepository.releaseStock(eq(1L), any(BigDecimal.class))).thenReturn(true);

        materialStockService.lock(1L, 2);
        materialStockService.release(1L, 2);

        verify(materialStockRepository).lockStock(eq(1L), any(BigDecimal.class));
        verify(materialStockRepository).releaseStock(eq(1L), any(BigDecimal.class));
    }

    @Test
    public void shouldAutoOutboundLockedStock() {
        when(materialStockRepository.queryById(1L)).thenReturn(stock());
        when(materialStockRepository.outboundLockedStock(eq(1L), any(BigDecimal.class))).thenReturn(true);

        materialStockService.autoOutbound(1L, 4, "流水线出库");

        verify(materialStockRepository).outboundLockedStock(eq(1L), any(BigDecimal.class));
    }

    private MaterialStockAggregate stock() {
        return MaterialStockAggregate.builder()
                .id(1L)
                .materialId(1L)
                .storageAddress("A-01")
                .totalQty(new BigDecimal("10.00"))
                .lockedQty(new BigDecimal("2.00"))
                .availableQty(new BigDecimal("8.00"))
                .build();
    }
}
