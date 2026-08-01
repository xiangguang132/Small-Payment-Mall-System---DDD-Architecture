package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.MaterialStockAllocation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IMaterialStockAllocationDao {

    void insert(MaterialStockAllocation allocation);

    MaterialStockAllocation queryById(@Param("id") Long id);

    MaterialStockAllocation queryByAllocationNo(@Param("allocationNo") String allocationNo);

    void update(MaterialStockAllocation allocation);

    List<MaterialStockAllocation> queryByStatus(@Param("status") Integer status,
                                                @Param("offset") int offset,
                                                @Param("pageSize") Integer pageSize);
}
