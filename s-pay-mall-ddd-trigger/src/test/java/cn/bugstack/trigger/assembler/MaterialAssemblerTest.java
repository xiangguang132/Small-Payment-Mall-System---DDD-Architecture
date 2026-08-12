package cn.bugstack.trigger.assembler;

import cn.bugstack.api.request.material.MaterialAddRequest;
import cn.bugstack.api.request.material.MaterialUpdateRequest;
import cn.bugstack.api.response.material.MaterialDetailResponse;
import cn.bugstack.domain.material.model.aggregate.MaterialAggregate;
import cn.bugstack.types.exception.AppException;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class MaterialAssemblerTest {

    @Test
    public void shouldMapAddRequestToAggregate() {
        MaterialAddRequest request = new MaterialAddRequest();
        request.setMaterialCode(" M001 ");
        request.setName(" steel ");
        request.setTypeId(1L);
        request.setUnit(" kg ");
        request.setDescription(" desc ");
        request.setStatus(1);

        MaterialAggregate aggregate = MaterialAssembler.toAggregate(request);

        assertEquals("M001", aggregate.getMaterialCode());
        assertEquals("steel", aggregate.getName());
        assertEquals("kg", aggregate.getUnit());
        assertEquals(Integer.valueOf(1), aggregate.getStatus());
    }

    @Test
    public void shouldMergeUpdatedFieldsWithCurrentAggregate() {
        MaterialAggregate current = MaterialAggregate.builder()
                .id(1L)
                .materialCode("M001")
                .name("old")
                .typeId(1L)
                .unit("kg")
                .description("old desc")
                .status(1)
                .isDel(0)
                .createTime(LocalDateTime.now())
                .build();
        MaterialUpdateRequest request = new MaterialUpdateRequest();
        request.setName(" new ");
        request.setTypeId(2L);

        MaterialAggregate updated = MaterialAssembler.toUpdatedAggregate(current, request);

        assertEquals("M001", updated.getMaterialCode());
        assertEquals("new", updated.getName());
        assertEquals(Long.valueOf(2L), updated.getTypeId());
        assertEquals("kg", updated.getUnit());
        assertEquals("old desc", updated.getDescription());
    }

    @Test(expected = AppException.class)
    public void shouldRejectNullAddRequest() {
        MaterialAssembler.toAggregate(null);
    }

    @Test
    public void shouldMapAggregateToDetailResponse() {
        MaterialAggregate aggregate = MaterialAggregate.builder()
                .id(1L)
                .materialCode("M001")
                .name("steel")
                .typeId(1L)
                .unit("kg")
                .status(1)
                .build();

        MaterialDetailResponse response = MaterialAssembler.toDetailResponse(aggregate);

        assertEquals(Long.valueOf(1L), response.getId());
        assertEquals("M001", response.getMaterialCode());
        assertEquals("kg", response.getUnit());
    }
}
