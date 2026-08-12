package cn.bugstack.trigger.assembler;

import cn.bugstack.api.response.materialstock.MaterialStockDetailResponse;
import cn.bugstack.api.response.materialstock.MaterialStockManualOutboundResponse;
import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MaterialStockAssemblerTest {

    @Test
    public void shouldMapStockToDetailResponse() {
        MaterialStockAggregate stock = stock();

        MaterialStockDetailResponse response = MaterialStockAssembler.toDetailResponse(stock);

        assertEquals(Long.valueOf(1L), response.getId());
        assertEquals(Long.valueOf(2L), response.getMaterialId());
        assertEquals("A-01", response.getStorageAddress());
        assertEquals(new BigDecimal("8.00"), response.getAvailableQty());
    }

    @Test
    public void shouldMapStockToManualOutboundResponse() {
        MaterialStockManualOutboundResponse response =
                MaterialStockAssembler.toManualOutboundResponse(stock());

        assertEquals("A-01", response.getStorageAddress());
        assertEquals(new BigDecimal("8.00"), response.getAvailableQty());
    }

    @Test
    public void shouldReturnNullForNullStock() {
        assertNull(MaterialStockAssembler.toDetailResponse(null));
        assertNull(MaterialStockAssembler.toManualOutboundResponse(null));
    }

    private MaterialStockAggregate stock() {
        return MaterialStockAggregate.builder()
                .id(1L)
                .materialId(2L)
                .storageAddress("A-01")
                .availableQty(new BigDecimal("8.00"))
                .lockedQty(new BigDecimal("2.00"))
                .totalQty(new BigDecimal("10.00"))
                .build();
    }
}
