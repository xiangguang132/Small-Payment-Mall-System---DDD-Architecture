package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.material.MaterialStockAllocationItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IMaterialStockAllocationItemDao {

    void insert(MaterialStockAllocationItem item);

    MaterialStockAllocationItem queryById(@Param("id") Long id);

    List<MaterialStockAllocationItem> queryByAllocationId(@Param("allocationId") Long allocationId);

    void update(MaterialStockAllocationItem item);

}
