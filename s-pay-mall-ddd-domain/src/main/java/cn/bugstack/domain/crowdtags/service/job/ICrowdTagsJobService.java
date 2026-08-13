package cn.bugstack.domain.crowdtags.service.job;

import cn.bugstack.domain.crowdtags.model.entity.CrowdTagsJobEntity;

import java.util.List;

public interface ICrowdTagsJobService {

    Long addCrowdTagJob(CrowdTagsJobEntity crowdTagJob);

    CrowdTagsJobEntity queryCrowdTagJobById(Long id);

    List<CrowdTagsJobEntity> queryCrowdTagJob(String tagId,
                                              String batchId,
                                              Integer tagType,
                                              String tagRule,
                                              int pageNo,
                                              int pageSize);

    long countCrowdTagJob(String tagId,
                          String batchId,
                          Integer tagType,
                          String tagRule);

    void operateSwitch(Long id, boolean open);

    void manualExecute(Long id);

    void deleteCrowdTagsJobById(Long id);
}
