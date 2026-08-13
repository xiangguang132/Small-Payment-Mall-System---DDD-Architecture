package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.crowdtags.model.entity.CrowdTagsJobEntity;
import cn.bugstack.domain.crowdtags.repository.job.ICrowdTagsJobRepository;
import cn.bugstack.infrastructure.dao.ICrowdTagsJobDao;
import cn.bugstack.infrastructure.dao.po.crowdtags.CrowdTagsJob;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class CrowdTagsJobRepository implements ICrowdTagsJobRepository {

    @Resource
    private ICrowdTagsJobDao crowdTagsJobDao;

    @Override
    public Long save(CrowdTagsJobEntity crowdTagJob) {
        CrowdTagsJob po = CrowdTagsJob.builder()
                .tagId(crowdTagJob.getTagId().trim())
                .batchId(crowdTagJob.getBatchId())
                .tagType(crowdTagJob.getTagType())
                .tagRule(crowdTagJob.getTagRule().trim())
                .statStartTime(crowdTagJob.getStatStartTime())
                .statEndTime(crowdTagJob.getStatEndTime())
                .lastExecuteTime(crowdTagJob.getLastExecuteTime())
                .status(crowdTagJob.getStatus())
                .isDel(crowdTagJob.getIsDel())
                .createTime(crowdTagJob.getCreateTime())
                .updateTime(crowdTagJob.getUpdateTime())
                .build();
        crowdTagsJobDao.insert(po);
        return po.getId();
    }

    @Override
    public CrowdTagsJobEntity queryById(Long id) {
        return toEntity(crowdTagsJobDao.queryById(id));
    }

    private static CrowdTagsJobEntity toEntity(CrowdTagsJob po) {
        if (po == null) {
            return null;
        }
        return CrowdTagsJobEntity.builder()
                .id(po.getId() == null ? null : po.getId().intValue())
                .tagId(po.getTagId())
                .batchId(po.getBatchId())
                .tagType(po.getTagType())
                .tagRule(po.getTagRule())
                .statStartTime(po.getStatStartTime())
                .statEndTime(po.getStatEndTime())
                .lastExecuteTime(po.getLastExecuteTime())
                .status(po.getStatus())
                .isDel(po.getIsDel())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

}
