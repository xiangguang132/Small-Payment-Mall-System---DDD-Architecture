package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.Warehouse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IWarehouseDao {

    void insert(Warehouse warehouse);

    void deleteById(@Param("id") Long id);

    Warehouse queryById(@Param("id") Long id);

    void update(Warehouse warehouse);
}
