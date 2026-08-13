package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.crowdtags.CrowdTags;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ICrowdTagsDao {

    void insert(CrowdTags crowdTags);

    void update(CrowdTags crowdTags);

    void deleteById(@Param("id") Long id);

    CrowdTags queryById(@Param("id") Long id);

    CrowdTags queryByTagId(@Param("tagId") String tagId);

    List<CrowdTags> queryPage(@Param("tagId") String tagId,
                              @Param("tagName") String tagName,
                              @Param("offset") int offset,
                              @Param("limit") int limit);

    long count(@Param("tagId") String tagId,
               @Param("tagName") String tagName);

    void updateCrowdTagsStatistics(CrowdTags crowdTagsReq);
}
