package cn.bugstack.infrastructure.repository;

import cn.bugstack.infrastructure.redis.IRedisService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RBitSet;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class GroupBuyActivityRepositoryTest {

    @Mock
    private IRedisService redisService;

    @Mock
    private RBitSet bitSet;

    private GroupBuyActivityRepository repository;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        repository = new GroupBuyActivityRepository();
        ReflectionTestUtils.setField(repository, "redisService", redisService);
    }

    @Test
    public void shouldAllowWhenTagIdIsBlank() {
        assertTrue(repository.withinTagCrowdRange(null, "u1"));
        assertTrue(repository.withinTagCrowdRange("", "u1"));

        verifyNoInteractions(redisService);
    }

    @Test
    public void shouldAllowWhenBitSetDoesNotExist() {
        when(redisService.getBitSet("tag-1")).thenReturn(bitSet);
        when(bitSet.isExists()).thenReturn(false);

        assertTrue(repository.withinTagCrowdRange("tag-1", "u1"));

        verify(redisService).getBitSet("tag-1");
        verify(bitSet).isExists();
        verify(bitSet, never()).get(anyInt());
    }

    @Test
    public void shouldReturnTrueWhenBitSetContainsUser() {
        when(redisService.getBitSet("tag-1")).thenReturn(bitSet);
        when(bitSet.isExists()).thenReturn(true);
        when(redisService.getIndexFromUserId("u1")).thenReturn(123);
        when(bitSet.get(123)).thenReturn(true);

        assertTrue(repository.withinTagCrowdRange("tag-1", "u1"));

        verify(redisService).getIndexFromUserId("u1");
        verify(bitSet).get(123);
    }

    @Test
    public void shouldReturnFalseWhenBitSetDoesNotContainUser() {
        when(redisService.getBitSet("tag-1")).thenReturn(bitSet);
        when(bitSet.isExists()).thenReturn(true);
        when(redisService.getIndexFromUserId("u1")).thenReturn(456);
        when(bitSet.get(456)).thenReturn(false);

        assertFalse(repository.withinTagCrowdRange("tag-1", "u1"));

        verify(redisService).getIndexFromUserId("u1");
        verify(bitSet).get(456);
    }
}
