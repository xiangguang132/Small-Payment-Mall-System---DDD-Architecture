package cn.bugstack.trigger.assembler;

import cn.bugstack.api.request.warehouse.WarehouseAddRequest;
import cn.bugstack.api.response.warehouse.WarehouseDetailResponse;
import cn.bugstack.domain.warehouse.model.aggregate.WarehouseAggregate;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WarehouseAssemblerTest {

    @Test
    public void shouldMapAddRequestToAggregate() {
        WarehouseAddRequest request = new WarehouseAddRequest();
        request.setWarehouseCode(" W001 ");
        request.setName(" main ");
        request.setType(0);
        request.setAddress(" addr ");
        request.setContactName(" tom ");
        request.setContactPhone(" 123 ");
        request.setStatus(1);

        WarehouseAggregate aggregate = WarehouseAssembler.toAggregate(request);

        assertEquals("W001", aggregate.getWarehouseCode());
        assertEquals("main", aggregate.getName());
        assertEquals(Integer.valueOf(0), aggregate.getType());
    }

    @Test
    public void shouldMapAggregateToDetailResponse() {
        WarehouseAggregate aggregate = WarehouseAggregate.builder()
                .id(1L)
                .warehouseCode("W001")
                .name("main")
                .type(0)
                .status(1)
                .build();

        WarehouseDetailResponse response = WarehouseAssembler.toDetailResponse(aggregate);

        assertEquals("W001", response.getWarehouseCode());
        assertEquals("main", response.getName());
        assertEquals(Integer.valueOf(0), response.getType());
    }
}
