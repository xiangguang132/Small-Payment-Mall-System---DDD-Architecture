package cn.bugstack.infrastructure.dao;

import cn.bugstack.Application;
import cn.bugstack.infrastructure.dao.po.CrowdTags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
public class CrowdTagsManagementDaoTest {

    @MockBean
    private RedissonClient redissonClient;

    @Autowired
    private ICrowdTagsDao crowdTagsDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void shouldInsertAndQueryCrowdTag() {
        CrowdTags tag = crowdTag("shouldInsertAndQueryCrowdTag");

        crowdTagsDao.insert(tag);

        CrowdTags byId = crowdTagsDao.queryById(tag.getId());
        CrowdTags byTagId = crowdTagsDao.queryByTagId(tag.getTagId());

        assertNotNull(byId);
        assertEquals(tag.getTagId(), byId.getTagId());
        assertEquals(tag.getTagName(), byTagId.getTagName());
        assertEquals(Integer.valueOf(0), byId.getIsDel());
    }

    @Test(expected = DuplicateKeyException.class)
    public void shouldRejectDuplicateTagId() {
        CrowdTags tag = crowdTag("shouldRejectDuplicateTagId");
        crowdTagsDao.insert(tag);

        crowdTagsDao.insert(crowdTag(tag.getTagId(), "other"));
    }

    @Test
    public void shouldUpdateCrowdTagMetadata() {
        CrowdTags tag = crowdTag("shouldUpdateCrowdTagMetadata");
        crowdTagsDao.insert(tag);
        tag.setTagName("Gold");
        tag.setTagDesc("new desc");

        crowdTagsDao.update(tag);

        CrowdTags found = crowdTagsDao.queryById(tag.getId());
        assertEquals("Gold", found.getTagName());
        assertEquals("new desc", found.getTagDesc());
    }

    @Test
    public void shouldSoftDeleteCrowdTag() {
        CrowdTags tag = crowdTag("shouldDeleteCrowdTag");
        crowdTagsDao.insert(tag);

        crowdTagsDao.deleteById(tag.getId());

        Integer isDel = jdbcTemplate.queryForObject(
                "select is_del from crowd_tags where id = ?",
                Integer.class,
                tag.getId()
        );

        assertEquals(Integer.valueOf(1), isDel);
        assertNull(crowdTagsDao.queryById(tag.getId()));
        assertEquals(0L, crowdTagsDao.count(tag.getTagId(), null));
        assertEquals(0, crowdTagsDao.queryPage(tag.getTagId(), null, 0, 10).size());
    }

    @Test
    public void shouldPageAndCountByTagNameKeyword() {
        String namePrefix = "VIP-" + System.nanoTime();
        crowdTagsDao.insert(crowdTag(namePrefix + "-1", namePrefix + "-A"));
        crowdTagsDao.insert(crowdTag(namePrefix + "-2", namePrefix + "-B"));

        long total = crowdTagsDao.count(null, namePrefix);
        List<CrowdTags> page = crowdTagsDao.queryPage(null, namePrefix, 0, 1);

        assertEquals(2L, total);
        assertEquals(1, page.size());
        assertEquals(namePrefix, page.get(0).getTagName().substring(0, namePrefix.length()));
    }

    private CrowdTags crowdTag(String testName) {
        return crowdTag("TAG_" + System.nanoTime(), testName + "-" + System.nanoTime());
    }

    private CrowdTags crowdTag(String tagId, String tagName) {
        LocalDateTime now = LocalDateTime.now();
        return CrowdTags.builder()
                .tagId(tagId)
                .tagName(tagName)
                .tagDesc("desc")
                .statistics(0)
                .createTime(now)
                .updateTime(now)
                .build();
    }
}
