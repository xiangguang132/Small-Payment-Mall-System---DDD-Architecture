package cn.bugstack.trigger.assembler;

import cn.bugstack.api.request.materialtype.MaterialTypeAddRequest;
import cn.bugstack.api.request.materialtype.MaterialTypeUpdateRequest;
import cn.bugstack.api.response.materialtype.MaterialTypeDetailResponse;
import cn.bugstack.domain.materialtype.model.aggregate.MaterialTypeAggregate;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MaterialTypeAssemblerTest {

    @Test
    public void shouldMapAddRequestToAggregate() {
        MaterialTypeAddRequest request = new MaterialTypeAddRequest();
        request.setParentId(0L);
        request.setName(" steel type ");
        request.setTypeCode(" T001 ");
        request.setSort(1);
        request.setStatus(1);

        MaterialTypeAggregate aggregate = MaterialTypeAssembler.toAggregate(request);

        assertEquals("steel type", aggregate.getName());
        assertEquals("T001", aggregate.getTypeCode());
        assertEquals(Integer.valueOf(1), aggregate.getSort());
    }

    @Test
    public void shouldMergeUpdatedFields() {
        MaterialTypeAggregate current = MaterialTypeAggregate.builder()
                .id(1L)
                .parentId(0L)
                .name("old")
                .typeCode("T001")
                .sort(1)
                .status(1)
                .build();
        MaterialTypeUpdateRequest request = new MaterialTypeUpdateRequest();
        request.setName(" new ");

        MaterialTypeAggregate updated = MaterialTypeAssembler.toUpdatedAggregate(current, request);

        assertEquals("new", updated.getName());
        assertEquals("T001", updated.getTypeCode());
    }

    @Test
    public void shouldMapAggregateToDetailResponse() {
        MaterialTypeDetailResponse response = MaterialTypeAssembler.toDetailResponse(
                MaterialTypeAggregate.builder().id(1L).name("steel").typeCode("T001").build()
        );

        assertEquals(Long.valueOf(1L), response.getId());
        assertEquals("steel", response.getName());
    }
}
