package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.ProductionOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IProductionOrderDao {

    void insert(ProductionOrder order);

    ProductionOrder queryById(@Param("id") Long id);
}
