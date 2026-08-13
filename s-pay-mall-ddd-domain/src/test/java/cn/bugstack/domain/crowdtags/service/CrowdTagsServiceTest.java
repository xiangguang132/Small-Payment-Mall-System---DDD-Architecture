package cn.bugstack.domain.crowdtags.service;

import cn.bugstack.domain.crowdtags.model.aggregate.CrowdTagsAggregate;
import cn.bugstack.domain.crowdtags.repository.tag.ICrowdTagsRepository;
import cn.bugstack.domain.crowdtags.service.tag.CrowdTagsService;
import cn.bugstack.types.exception.AppException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CrowdTagsServiceTest {

    @Mock
    private ICrowdTagsRepository crowdTagsRepository;

    @InjectMocks
    private CrowdTagsService crowdTagsService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldAddCrowdTag() {
        CrowdTagsAggregate tag = CrowdTagsAggregate.create("TAG001", "VIP", "vip users");
        when(crowdTagsRepository.queryByTagId("TAG001")).thenReturn(null);
        when(crowdTagsRepository.save(tag)).thenReturn(1L);

        assertEquals(Long.valueOf(1L), crowdTagsService.addCrowdTag(tag));
        verify(crowdTagsRepository).save(tag);
    }

    @Test(expected = AppException.class)
    public void shouldRejectDuplicateTagId() {
        CrowdTagsAggregate tag = CrowdTagsAggregate.create("TAG001", "VIP", "vip users");
        when(crowdTagsRepository.queryByTagId("TAG001")).thenReturn(tag);

        crowdTagsService.addCrowdTag(tag);
    }

    @Test(expected = AppException.class)
    public void shouldRejectBlankTagId() {
        crowdTagsService.addCrowdTag(CrowdTagsAggregate.create(" ", "VIP", ""));
    }

    @Test(expected = AppException.class)
    public void shouldRejectDeleteWhenActivityUsesTag() {
        CrowdTagsAggregate current = CrowdTagsAggregate.builder()
                .id(1L)
                .tagId("TAG001")
                .tagName("VIP")
                .build();
        when(crowdTagsRepository.queryById(1L)).thenReturn(current);
        when(crowdTagsRepository.countActivityByTagId("TAG001")).thenReturn(1L);

        crowdTagsService.deleteCrowdTagById(1L);
    }

    @Test
    public void shouldDeleteWhenNotReferenced() {
        CrowdTagsAggregate current = CrowdTagsAggregate.builder()
                .id(1L)
                .tagId("TAG001")
                .tagName("VIP")
                .build();
        when(crowdTagsRepository.queryById(1L)).thenReturn(current);
        when(crowdTagsRepository.countActivityByTagId("TAG001")).thenReturn(0L);
        when(crowdTagsRepository.countDiscountByTagId("TAG001")).thenReturn(0L);

        crowdTagsService.deleteCrowdTagById(1L);

        verify(crowdTagsRepository).deleteById(1L);
    }

    @Test
    public void shouldUpdateWithoutChangingTagIdOrStatistics() {
        CrowdTagsAggregate current = CrowdTagsAggregate.builder()
                .id(1L)
                .tagId("TAG001")
                .tagName("VIP")
                .tagDesc("old")
                .statistics(5)
                .build();
        CrowdTagsAggregate updated = CrowdTagsAggregate.builder()
                .id(1L)
                .tagId("TAG999")
                .tagName("Gold")
                .tagDesc("new")
                .statistics(999)
                .build();
        when(crowdTagsRepository.queryById(1L)).thenReturn(current);

        CrowdTagsAggregate result = crowdTagsService.updateCrowdTag(1L, updated);

        assertEquals("TAG001", result.getTagId());
        assertEquals("Gold", result.getTagName());
        assertEquals(Integer.valueOf(5), result.getStatistics());
        verify(crowdTagsRepository).update(result);
    }
}
