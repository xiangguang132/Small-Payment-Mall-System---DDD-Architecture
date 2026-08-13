package cn.bugstack.domain.crowdtags.repository.job;

import cn.bugstack.domain.crowdtags.model.entity.CrowdTagsJobEntity;

public interface ICrowdTagsJobRepository {

    Long save(CrowdTagsJobEntity crowdTagJob);

    CrowdTagsJobEntity queryById(Long id);

}
