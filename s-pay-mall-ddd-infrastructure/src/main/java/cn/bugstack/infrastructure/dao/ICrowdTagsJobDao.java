package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.crowdtags.CrowdTagsJob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ICrowdTagsJobDao {

    void insert(CrowdTagsJob crowdTagsJob);

    CrowdTagsJob queryCrowdTagsJob(CrowdTagsJob crowdTagsJobReq);

    CrowdTagsJob queryById(@Param("id") Long id);

    List<CrowdTagsJob> queryPage(@Param("tagId") String tagId,
                                 @Param("batchId") String batchId,
                                 @Param("tagType") Integer tagType,
                                 @Param("tagRule") String tagRule,
                                 @Param("offset") int offset,
                                 @Param("limit") int limit);

    long count(@Param("tagId") String tagId,
               @Param("batchId") String batchId,
               @Param("tagType") Integer tagType,
               @Param("tagRule") String tagRule);

    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    void deleteById(Long id);
}
