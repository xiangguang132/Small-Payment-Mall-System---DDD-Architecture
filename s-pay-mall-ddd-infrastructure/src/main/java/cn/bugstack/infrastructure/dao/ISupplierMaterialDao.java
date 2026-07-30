package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.SupplierMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ISupplierMaterialDao {

    SupplierMaterial queryById(@Param("id") Long id);
}
