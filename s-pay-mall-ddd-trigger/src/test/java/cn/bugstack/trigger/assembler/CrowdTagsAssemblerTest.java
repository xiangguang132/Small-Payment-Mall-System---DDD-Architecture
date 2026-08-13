package cn.bugstack.trigger.assembler;

import cn.bugstack.api.request.crowdtags.CrowdTagsAddRequest;
import cn.bugstack.api.request.crowdtags.CrowdTagsUpdateRequest;
import cn.bugstack.api.response.crowdtags.CrowdTagsDetailResponse;
import cn.bugstack.domain.tag.model.aggregate.CrowdTagsAggregate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CrowdTagsAssemblerTest {

    @Test
    public void shouldMapAddRequestToAggregate() {
        CrowdTagsAddRequest request = new CrowdTagsAddRequest();
        request.setTagId(" TAG001 ");
        request.setTagName(" VIP ");
        request.setTagDesc(" vip users ");

        CrowdTagsAggregate aggregate = CrowdTagsAssembler.toAggregate(request);

        assertEquals("TAG001", aggregate.getTagId());
        assertEquals("VIP", aggregate.getTagName());
        assertEquals("vip users", aggregate.getTagDesc());
        assertEquals(Integer.valueOf(0), aggregate.getStatistics());
    }

    @Test
    public void shouldMergeUpdatedFields() {
        CrowdTagsAggregate current = CrowdTagsAggregate.builder()
                .id(1L)
                .tagId("TAG001")
                .tagName("VIP")
                .tagDesc("old")
                .statistics(3)
                .build();
        CrowdTagsUpdateRequest request = new CrowdTagsUpdateRequest();
        request.setTagName(" Gold ");
        request.setTagDesc(" gold users ");

        CrowdTagsAggregate updated = CrowdTagsAssembler.toUpdatedAggregate(current, request);

        assertEquals("TAG001", updated.getTagId());
        assertEquals("Gold", updated.getTagName());
        assertEquals("gold users", updated.getTagDesc());
        assertEquals(Integer.valueOf(3), updated.getStatistics());
    }

    @Test
    public void shouldMapAggregateToDetailResponse() {
        CrowdTagsDetailResponse response = CrowdTagsAssembler.toDetailResponse(
                CrowdTagsAggregate.builder()
                        .id(1L)
                        .tagId("TAG001")
                        .tagName("VIP")
                        .tagDesc("vip users")
                        .statistics(3)
                        .build()
        );

        assertEquals("TAG001", response.getTagId());
        assertEquals("VIP", response.getTagName());
    }

    @Test
    public void shouldExposeOnlyUsefulFieldsAsJson() throws Exception {
        CrowdTagsDetailResponse response = CrowdTagsAssembler.toDetailResponse(
                CrowdTagsAggregate.builder()
                        .id(1L)
                        .tagId("TAG001")
                        .tagName("VIP")
                        .tagDesc("vip users")
                        .statistics(3)
                        .build()
        );

        JsonNode json = new ObjectMapper().readTree(
                new ObjectMapper().writeValueAsString(response)
        );

        assertEquals(3, json.size());
        assertEquals("TAG001", json.get("tagId").asText());
        assertEquals("VIP", json.get("tagName").asText());
        assertEquals("vip users", json.get("tagDesc").asText());
        assertFalse(json.has("id"));
        assertFalse(json.has("statistics"));
        assertFalse(json.has("createTime"));
        assertFalse(json.has("updateTime"));
        assertTrue(json.has("tagId"));
    }
}
