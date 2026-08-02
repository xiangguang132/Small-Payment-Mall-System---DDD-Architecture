package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.ProductionOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IProductionOrderDao {

    void insert(ProductionOrder order);
}
