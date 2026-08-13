package cn.bugstack.domain.crowdtags.service.job;

import cn.bugstack.domain.crowdtags.model.entity.CrowdTagsJobEntity;

public interface ICrowdTagsJobService {

    Long addCrowdTagJob(CrowdTagsJobEntity crowdTagJob);

    CrowdTagsJobEntity queryCrowdTagJobById(Long id);

}
