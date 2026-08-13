package cn.bugstack.domain.tag.repository;

import cn.bugstack.domain.tag.model.aggregate.CrowdTagsAggregate;

import java.util.List;

public interface ICrowdTagsRepository {

    Long save(CrowdTagsAggregate crowdTag);

    void update(CrowdTagsAggregate crowdTag);

    void deleteById(Long id);

    CrowdTagsAggregate queryById(Long id);

    CrowdTagsAggregate queryByTagId(String tagId);

    List<CrowdTagsAggregate> queryPage(String tagId, String tagName, int offset, int limit);

    long count(String tagId, String tagName);

    long countActivityByTagId(String tagId);

    long countDiscountByTagId(String tagId);
}
