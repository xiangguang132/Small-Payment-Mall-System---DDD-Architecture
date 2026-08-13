package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.crowdtags.model.aggregate.CrowdTagsAggregate;
import cn.bugstack.domain.crowdtags.repository.tag.ICrowdTagsRepository;
import cn.bugstack.infrastructure.dao.ICrowdTagsDao;
import cn.bugstack.infrastructure.dao.IGroupBuyActivityDao;
import cn.bugstack.infrastructure.dao.IGroupBuyDiscountDao;
import cn.bugstack.infrastructure.dao.po.crowdtags.CrowdTags;
import cn.bugstack.infrastructure.redis.IRedisService;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CrowdTagsRepository implements ICrowdTagsRepository {

    @Resource
    private ICrowdTagsDao crowdTagsDao;

    @Resource
    private IGroupBuyActivityDao groupBuyActivityDao;

    @Resource
    private IGroupBuyDiscountDao groupBuyDiscountDao;

    @Resource
    private IRedisService redisService;

    @Override
    public Long save(CrowdTagsAggregate crowdTag) {
        CrowdTags po = CrowdTags.builder()
                .tagId(crowdTag.getTagId().trim())
                .tagName(crowdTag.getTagName().trim())
                .tagDesc(crowdTag.getTagDesc() == null ? "" : crowdTag.getTagDesc().trim())
                .statistics(crowdTag.getStatistics() == null ? 0 : crowdTag.getStatistics())
                .isDel(0)
                .createTime(crowdTag.getCreateTime())
                .updateTime(crowdTag.getUpdateTime())
                .build();
        crowdTagsDao.insert(po);
        return po.getId();
    }

    @Override
    public void update(CrowdTagsAggregate crowdTag) {
        CrowdTags po = CrowdTags.builder()
                .id(crowdTag.getId())
                .tagName(crowdTag.getTagName().trim())
                .tagDesc(crowdTag.getTagDesc() == null ? "" : crowdTag.getTagDesc().trim())
                .build();
        crowdTagsDao.update(po);
    }

    @Override
    public void deleteById(Long id) {
        CrowdTags current = crowdTagsDao.queryById(id);
        if (current == null) {
            return;
        }
        crowdTagsDao.deleteById(id);
        redisService.delete(current.getTagId());
    }

    @Override
    public CrowdTagsAggregate queryById(Long id) {
        return toAggregate(crowdTagsDao.queryById(id));
    }

    @Override
    public CrowdTagsAggregate queryByTagId(String tagId) {
        return toAggregate(crowdTagsDao.queryByTagId(tagId));
    }

    @Override
    public List<CrowdTagsAggregate> queryPage(String tagId, String tagName, int offset, int limit) {
        return crowdTagsDao.queryPage(tagId, tagName, offset, limit).stream()
                .map(CrowdTagsRepository::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public long count(String tagId, String tagName) {
        return crowdTagsDao.count(tagId, tagName);
    }

    @Override
    public long countActivityByTagId(String tagId) {
        return groupBuyActivityDao.countByTagId(tagId);
    }

    @Override
    public long countDiscountByTagId(String tagId) {
        return groupBuyDiscountDao.countByTagId(tagId);
    }

    private static CrowdTagsAggregate toAggregate(CrowdTags po) {
        if (po == null) {
            return null;
        }
        return CrowdTagsAggregate.builder()
                .id(po.getId())
                .tagId(po.getTagId())
                .tagName(po.getTagName())
                .tagDesc(po.getTagDesc())
                .statistics(po.getStatistics())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
}
