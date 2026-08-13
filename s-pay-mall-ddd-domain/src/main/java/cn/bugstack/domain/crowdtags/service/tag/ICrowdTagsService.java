package cn.bugstack.domain.crowdtags.service.tag;

import cn.bugstack.domain.crowdtags.model.aggregate.CrowdTagsAggregate;

import java.util.List;

public interface ICrowdTagsService {

    Long addCrowdTag(CrowdTagsAggregate crowdTag);

    void deleteCrowdTagById(Long id);

    CrowdTagsAggregate queryCrowdTagById(Long id);

    CrowdTagsAggregate updateCrowdTag(Long id, CrowdTagsAggregate updated);

    List<CrowdTagsAggregate> queryCrowdTags(String tagId, String tagName, int pageNo, int pageSize);

    long countCrowdTags(String tagId, String tagName);
}
