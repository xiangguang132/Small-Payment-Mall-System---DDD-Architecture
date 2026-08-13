package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.crowdtags.model.entity.CrowdTagsJobEntity;
import cn.bugstack.domain.crowdtags.repository.job.ICrowdTagsJobRepository;
import cn.bugstack.infrastructure.dao.ICrowdTagsJobDao;
import cn.bugstack.infrastructure.dao.po.crowdtags.CrowdTagsJob;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public List<CrowdTagsJobEntity> queryPage(String tagId,
                                              String batchId,
                                              Integer tagType,
                                              String tagRule,
                                              int offset,
                                              int limit) {
        return crowdTagsJobDao.queryPage(tagId, batchId, tagType, tagRule, offset, limit).stream()
                .map(CrowdTagsJobRepository::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public long count(String tagId,
                      String batchId,
                      Integer tagType,
                      String tagRule) {
        return crowdTagsJobDao.count(tagId, batchId, tagType, tagRule);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        crowdTagsJobDao.updateStatus(id, status);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "任务呢标签repository：请求id不能为空");
        }
        crowdTagsJobDao.deleteById(id);
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
