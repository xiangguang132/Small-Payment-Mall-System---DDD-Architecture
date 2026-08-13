package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.warehouse.WarehouseStockFlow;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IWarehouseStockFlowDao {

    int insert(WarehouseStockFlow flow);

}
