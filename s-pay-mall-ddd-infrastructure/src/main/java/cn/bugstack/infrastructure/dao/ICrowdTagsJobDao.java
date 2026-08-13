package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.crowdtags.CrowdTagsJob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ICrowdTagsJobDao {

    void insert(CrowdTagsJob crowdTagsJob);

    CrowdTagsJob queryCrowdTagsJob(CrowdTagsJob crowdTagsJobReq);

    CrowdTagsJob queryById(@Param("id") Long id);
}
