package cn.bugstack.trigger.assembler;

import cn.bugstack.api.request.producttype.ProductTypeAddRequest;
import cn.bugstack.api.request.producttype.ProductTypeUpdateRequest;
import cn.bugstack.api.response.producttype.ProductTypeDetailResponse;
import cn.bugstack.domain.producttype.model.aggregate.ProductTypeAggregate;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProductTypeAssemblerTest {

    @Test
    public void shouldMapAddRequestToAggregate() {
        ProductTypeAddRequest request = new ProductTypeAddRequest();
        request.setParentId(0L);
        request.setName(" food ");
        request.setTypeCode(" P001 ");
        request.setSort(2);
        request.setStatus(1);

        ProductTypeAggregate aggregate = ProductTypeAssembler.toAggregate(request);

        assertEquals("food", aggregate.getName());
        assertEquals("P001", aggregate.getTypeCode());
        assertEquals(Integer.valueOf(2), aggregate.getSort());
    }

    @Test
    public void shouldMergeUpdatedFields() {
        ProductTypeAggregate current = ProductTypeAggregate.builder()
                .id(1L)
                .parentId(0L)
                .name("old")
                .typeCode("P001")
                .sort(1)
                .status(1)
                .build();
        ProductTypeUpdateRequest request = new ProductTypeUpdateRequest();
        request.setStatus(0);

        ProductTypeAggregate updated = ProductTypeAssembler.toUpdatedAggregate(current, request);

        assertEquals(Integer.valueOf(0), updated.getStatus());
        assertEquals("P001", updated.getTypeCode());
    }

    @Test
    public void shouldMapAggregateToDetailResponse() {
        ProductTypeDetailResponse response = ProductTypeAssembler.toDetailResponse(
                ProductTypeAggregate.builder().id(1L).name("food").typeCode("P001").build()
        );

        assertEquals(Long.valueOf(1L), response.getId());
        assertEquals("food", response.getName());
    }
}
