package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.material.Material;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IMaterialDao {

    void insert(Material material);

    void deleteById(@Param("id") Long id);

    Material queryById(@Param("id") Long id);

    Material queryByMaterialCode(@Param("materialCode") String materialCode);

    void update(Material material);

    long countByTypeId(@Param("typeId") Long typeId);
}
