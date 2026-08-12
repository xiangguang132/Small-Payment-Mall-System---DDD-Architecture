package cn.bugstack.trigger.assembler;

import cn.bugstack.api.response.warehousestock.StockDetailResponse;
import cn.bugstack.domain.warehousestock.model.aggregate.StockAggregate;
import cn.bugstack.types.exception.AppException;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class StockAssemblerTest {

    @Test
    public void shouldMapStockToDetailResponse() {
        StockAggregate stock = StockAggregate.builder()
                .id(1L)
                .warehouseId(2L)
                .productId(3L)
                .availableQty(new BigDecimal("8.00"))
                .lockedQty(new BigDecimal("2.00"))
                .totalQty(new BigDecimal("10.00"))
                .build();

        StockDetailResponse response = StockAssembler.toDetailResponse(stock);

        assertEquals(Long.valueOf(1L), response.getId());
        assertEquals(Long.valueOf(2L), response.getWarehouseId());
        assertEquals(new BigDecimal("10.00"), response.getTotalQty());
    }

    @Test(expected = AppException.class)
    public void shouldRejectNullStock() {
        StockAssembler.toDetailResponse(null);
    }
}
