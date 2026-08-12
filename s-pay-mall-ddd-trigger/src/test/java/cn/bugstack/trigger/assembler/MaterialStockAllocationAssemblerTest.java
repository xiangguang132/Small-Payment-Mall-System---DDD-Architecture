package cn.bugstack.trigger.assembler;

import cn.bugstack.api.response.materialstockallocation.MaterialStockAllocationDetailResponse;
import cn.bugstack.api.response.materialstockallocation.MaterialStockAllocationItemResponse;
import cn.bugstack.api.response.materialstockallocation.MaterialStockAllocationStatusResponse;
import cn.bugstack.domain.materialstockallocation.model.aggregate.MaterialStockAllocationAggregate;
import cn.bugstack.domain.materialstockallocation.model.vo.MaterialStockAllocationItemVO;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MaterialStockAllocationAssemblerTest {

    @Test
    public void shouldMapAggregateToDetailResponse() {
        MaterialStockAllocationAggregate aggregate = aggregate();

        MaterialStockAllocationDetailResponse response =
                MaterialStockAllocationAssembler.toDetailResponse(aggregate);

        assertEquals("MSA001", response.getAllocationNo());
        assertEquals(1, response.getItems().size());
        MaterialStockAllocationItemResponse item = response.getItems().get(0);
        assertEquals(Long.valueOf(1L), item.getStockId());
        assertEquals(new BigDecimal("5.00"), item.getAllocateQty());
    }

    @Test
    public void shouldMapAggregateToStatusResponse() {
        MaterialStockAllocationStatusResponse response =
                MaterialStockAllocationAssembler.toStatusResponse(aggregate());

        assertEquals("MSA001", response.getAllocationNo());
        assertEquals(Integer.valueOf(0), response.getStatus());
    }

    @Test
    public void shouldReturnNullForNullAggregate() {
        assertNull(MaterialStockAllocationAssembler.toDetailResponse(null));
        assertNull(MaterialStockAllocationAssembler.toStatusResponse(null));
    }

    private MaterialStockAllocationAggregate aggregate() {
        return MaterialStockAllocationAggregate.builder()
                .allocationNo("MSA001")
                .materialId(2L)
                .requestQty(new BigDecimal("5.00"))
                .status(0)
                .items(Collections.singletonList(
                        MaterialStockAllocationItemVO.builder()
                                .stockId(1L)
                                .materialId(2L)
                                .storageAddress("A-01")
                                .allocateQty(new BigDecimal("5.00"))
                                .sortNo(1)
                                .build()
                ))
                .build();
    }
}
