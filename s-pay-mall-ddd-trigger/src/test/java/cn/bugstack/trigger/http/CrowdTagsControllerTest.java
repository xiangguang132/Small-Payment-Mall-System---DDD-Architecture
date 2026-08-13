package cn.bugstack.trigger.http;

import cn.bugstack.api.request.crowdtags.CrowdTagsAddRequest;
import cn.bugstack.api.request.crowdtags.CrowdTagsUpdateRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.crowdtags.CrowdTagsDetailResponse;
import cn.bugstack.domain.tag.model.aggregate.CrowdTagsAggregate;
import cn.bugstack.domain.tag.service.ICrowdTagsService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class CrowdTagsControllerTest {

    @Mock
    private ICrowdTagsService crowdTagsService;

    @InjectMocks
    private CrowdTagsController crowdTagsController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldReturnCreatedCrowdTagDetailOnAdd() {
        CrowdTagsAddRequest request = new CrowdTagsAddRequest();
        request.setTagId("TAG001");
        request.setTagName("VIP");
        request.setTagDesc("vip users");
        when(crowdTagsService.addCrowdTag(any(CrowdTagsAggregate.class))).thenReturn(1L);
        when(crowdTagsService.queryCrowdTagById(1L)).thenReturn(
                CrowdTagsAggregate.builder()
                        .id(1L)
                        .tagId("TAG001")
                        .tagName("VIP")
                        .tagDesc("vip users")
                        .statistics(0)
                        .build()
        );

        Response<CrowdTagsDetailResponse> response = crowdTagsController.add(request);

        assertEquals("TAG001", response.getData().getTagId());
        assertEquals("VIP", response.getData().getTagName());
    }

    @Test
    public void shouldReturnUpdatedCrowdTagDetailOnUpdate() {
        CrowdTagsUpdateRequest request = new CrowdTagsUpdateRequest();
        request.setTagName("Gold");
        request.setTagDesc("gold users");
        CrowdTagsAggregate current = CrowdTagsAggregate.builder()
                .id(1L)
                .tagId("TAG001")
                .tagName("VIP")
                .tagDesc("vip users")
                .statistics(0)
                .build();
        CrowdTagsAggregate updated = CrowdTagsAggregate.builder()
                .id(1L)
                .tagId("TAG001")
                .tagName("Gold")
                .tagDesc("gold users")
                .statistics(0)
                .build();
        when(crowdTagsService.queryCrowdTagById(1L)).thenReturn(current);
        when(crowdTagsService.updateCrowdTag(eq(1L), any(CrowdTagsAggregate.class))).thenReturn(updated);

        Response<CrowdTagsDetailResponse> response = crowdTagsController.update(1L, request);

        assertEquals("TAG001", response.getData().getTagId());
        assertEquals("Gold", response.getData().getTagName());
        assertEquals("gold users", response.getData().getTagDesc());
    }
}
