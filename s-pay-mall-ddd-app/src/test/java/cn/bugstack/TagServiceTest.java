package cn.bugstack;

import cn.bugstack.domain.tag.service.TagService;
import cn.bugstack.infrastructure.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RBitSet;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Slf4j
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = Application.class)
public class TagServiceTest {

    @Resource
    private TagService tagService;

    @Resource
    private IRedisService redisService;

    @Test
    public void test_tag_job() {
        tagService.execTagBatchJob("RQ_KJHKL98UU78H66554GFDV", "10001");

        RBitSet bitSet = redisService.getBitSet("RQ_KJHKL98UU78H66554GFDV");
        assertTrue(bitSet.get(redisService.getIndexFromUserId("xiaofuge")));
        assertTrue(bitSet.get(redisService.getIndexFromUserId("liergou")));
        assertFalse(bitSet.get(redisService.getIndexFromUserId("gudebai")));
    }
}
