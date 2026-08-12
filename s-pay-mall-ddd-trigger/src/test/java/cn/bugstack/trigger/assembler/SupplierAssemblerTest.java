package cn.bugstack.trigger.assembler;

import cn.bugstack.api.request.supplier.SupplierAddRequest;
import cn.bugstack.api.response.supplier.SupplierDetailResponse;
import cn.bugstack.domain.supplier.model.aggregate.SupplierAggregate;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SupplierAssemblerTest {

    @Test
    public void shouldMapAddRequestToAggregate() {
        SupplierAddRequest request = new SupplierAddRequest();
        request.setSupplierCode(" S001 ");
        request.setName(" supplier ");
        request.setContactName(" tom ");
        request.setContactPhone(" 123 ");
        request.setAddress(" addr ");
        request.setStatus(1);

        SupplierAggregate aggregate = SupplierAssembler.toAggregate(request);

        assertEquals("S001", aggregate.getSupplierCode());
        assertEquals("supplier", aggregate.getName());
        assertEquals("tom", aggregate.getContactName());
        assertEquals("123", aggregate.getContactPhone());
    }

    @Test
    public void shouldMapAggregateToDetailResponse() {
        SupplierAggregate aggregate = SupplierAggregate.builder()
                .id(1L)
                .supplierCode("S001")
                .name("supplier")
                .status(1)
                .build();

        SupplierDetailResponse response = SupplierAssembler.toDetailResponse(aggregate);

        assertEquals("S001", response.getSupplierCode());
        assertEquals("supplier", response.getName());
    }
}
