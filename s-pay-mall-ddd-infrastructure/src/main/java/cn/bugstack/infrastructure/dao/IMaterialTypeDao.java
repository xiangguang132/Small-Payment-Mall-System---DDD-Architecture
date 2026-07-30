package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.MaterialType;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IMaterialTypeDao {

    void insert(MaterialType materialType);
}
