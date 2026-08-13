package cn.bugstack.domain.crowdtags.service.job;

import cn.bugstack.domain.crowdtags.repository.job.ICrowdTagsJobRepository;
import cn.bugstack.types.exception.AppException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class CrowdTagsJobServiceTest {

    @Mock
    private ICrowdTagsJobRepository crowdTagsJobRepository;

    @InjectMocks
    private CrowdTagsJobService crowdTagsJobService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldOpenJobWithStatusOne() {
        crowdTagsJobService.operateSwitch(1L, true);

        verify(crowdTagsJobRepository).updateStatus(1L, Integer.valueOf(1));
    }

    @Test
    public void shouldCloseJobWithStatusTwo() {
        crowdTagsJobService.operateSwitch(1L, false);

        verify(crowdTagsJobRepository).updateStatus(1L, Integer.valueOf(2));
    }

    @Test
    public void shouldRejectNullId() {
        try {
            crowdTagsJobService.operateSwitch(null, true);
            fail("expected AppException");
        } catch (AppException ignored) {
            // expected
        }

        verifyNoInteractions(crowdTagsJobRepository);
    }
}
