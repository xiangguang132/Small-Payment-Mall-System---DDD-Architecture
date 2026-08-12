package cn.bugstack.domain.warehousestock.service;

import cn.bugstack.domain.warehousestock.model.aggregate.StockAggregate;
import cn.bugstack.domain.warehousestock.repository.IStockRepository;
import cn.bugstack.types.exception.AppException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StockServiceTest {

    @Mock
    private IStockRepository stockRepository;

    @InjectMocks
    private StockService stockService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = AppException.class)
    public void shouldRejectQueryWhenStockMissing() {
        when(stockRepository.queryById(1L)).thenReturn(null);

        stockService.queryStockById(1L);
    }

    @Test(expected = AppException.class)
    public void shouldRejectDuplicateStockRecord() {
        when(stockRepository.queryByWarehouseIdAndProductId(1L, 2L))
                .thenReturn(StockAggregate.builder().id(1L).build());

        stockService.addStock(1L, 2L);
    }

    @Test
    public void shouldCreateStockWhenMissingDuringAdjust() {
        when(stockRepository.queryByWarehouseIdAndProductId(1L, 2L)).thenReturn(null);
        when(stockRepository.save(org.mockito.ArgumentMatchers.any(StockAggregate.class)))
                .thenReturn(1L);

        stockService.adjustStock(1L, 2L, 5, "盘点");

        verify(stockRepository).save(org.mockito.ArgumentMatchers.any(StockAggregate.class));
    }

    @Test(expected = AppException.class)
    public void shouldRejectOutboundWhenAvailableLessThanLocked() {
        StockAggregate stock = StockAggregate.builder()
                .id(1L)
                .warehouseId(1L)
                .productId(2L)
                .totalQty(new BigDecimal("10.00"))
                .lockedQty(new BigDecimal("10.00"))
                .availableQty(BigDecimal.ZERO)
                .build();
        when(stockRepository.queryByWarehouseIdAndProductId(1L, 2L)).thenReturn(stock);

        stockService.outbound(1L, 2L, 1);
    }

    @Test
    public void shouldNotApplyInboundWhenFlowAlreadySaved() {
        when(stockRepository.saveFlow(1L, 2L, new BigDecimal("5"), "PRODUCTION_ORDER", "PO001", "生产单成品入库"))
                .thenReturn(false);

        stockService.inbound(1L, 2L, 5, "PRODUCTION_ORDER", "PO001");

        verify(stockRepository, never()).updateById(org.mockito.ArgumentMatchers.any(StockAggregate.class));
    }
}
