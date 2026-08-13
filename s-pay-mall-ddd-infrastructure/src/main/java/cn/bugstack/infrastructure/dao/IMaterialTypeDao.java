package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.material.MaterialType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IMaterialTypeDao {

    void insert(MaterialType materialType);

    void deleteById(@Param("id") Long id);

    MaterialType queryById(@Param("id") Long id);

    MaterialType queryByTypeCode(@Param("typeCode") String typeCode);

    void update(MaterialType materialType);

    long countByParentId(@Param("parentId") Long parentId);
}
