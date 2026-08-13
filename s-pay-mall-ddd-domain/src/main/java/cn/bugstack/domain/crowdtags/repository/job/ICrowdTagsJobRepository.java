package cn.bugstack.domain.crowdtags.repository.job;

import cn.bugstack.domain.crowdtags.model.entity.CrowdTagsJobEntity;

import java.util.List;

public interface ICrowdTagsJobRepository {

    Long save(CrowdTagsJobEntity crowdTagJob);

    CrowdTagsJobEntity queryById(Long id);

    List<CrowdTagsJobEntity> queryPage(String tagId,
                                       String batchId,
                                       Integer tagType,
                                       String tagRule,
                                       int offset,
                                       int limit);

    long count(String tagId,
               String batchId,
               Integer tagType,
               String tagRule);

    void updateStatus(Long id, Integer status);

    void deleteById(Long id);
}
