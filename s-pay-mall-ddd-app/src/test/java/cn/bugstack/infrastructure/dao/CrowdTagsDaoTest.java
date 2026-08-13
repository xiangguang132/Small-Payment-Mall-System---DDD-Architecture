package cn.bugstack.infrastructure.dao;

import cn.bugstack.Application;
import cn.bugstack.infrastructure.dao.po.crowdtags.CrowdTagsDetail;
import cn.bugstack.infrastructure.dao.po.crowdtags.CrowdTagsJob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = Application.class,
        properties = {
                "spring.autoconfigure.exclude=" +
                        "org.redisson.spring.starter.RedissonAutoConfiguration," +
                        "org.redisson.spring.starter.RedissonAutoConfigurationV2"
        }
)
@Transactional
public class CrowdTagsDaoTest {

    @MockBean
    private RedissonClient redissonClient;

    @Autowired
    private ICrowdTagsJobDao crowdTagsJobDao;

    @Autowired
    private ICrowdTagsDetailDao crowdTagsDetailDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void shouldQueryCrowdTagsJobByTagIdAndBatchId() {
        String tagId = "JOB_TEST_" + System.nanoTime();
        String batchId = "BATCH_TEST_" + System.nanoTime();

        jdbcTemplate.update(
                "insert into crowd_tags_job(" +
                        "tag_id, batch_id, tag_type, tag_rule, " +
                        "stat_start_time, stat_end_time, status) " +
                        "values (?, ?, ?, ?, ?, ?, ?)",
                tagId,
                batchId,
                1,
                "amount >= 100",
                Timestamp.valueOf(LocalDateTime.of(2026, 8, 1, 0, 0)),
                Timestamp.valueOf(LocalDateTime.of(2026, 8, 31, 23, 59)),
                0
        );

        CrowdTagsJob result = crowdTagsJobDao.queryCrowdTagsJob(
                CrowdTagsJob.builder()
                        .tagId(tagId)
                        .batchId(batchId)
                        .build()
        );

        assertNotNull(result);
        assertEquals(tagId, result.getTagId());
        assertEquals(batchId, result.getBatchId());
        assertEquals(Integer.valueOf(1), result.getTagType());
        assertEquals("amount >= 100", result.getTagRule());
        assertEquals(Integer.valueOf(0), result.getStatus());
    }

    @Test
    public void shouldInsertCrowdTagsDetail() {
        String tagId = "DETAIL_TEST_" + System.nanoTime();
        String userId = "user-" + System.nanoTime();

        crowdTagsDetailDao.addCrowdTagsUserId(
                CrowdTagsDetail.builder()
                        .tagId(tagId)
                        .userId(userId)
                        .build()
        );

        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from crowd_tags_detail where tag_id = ? and user_id = ?",
                Integer.class,
                tagId,
                userId
        );

        assertEquals(1, count.intValue());
    }
}
