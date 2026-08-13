package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.warehouse.WarehouseStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IWarehouseStockDao {

    void insert(WarehouseStock stock);

    WarehouseStock queryById(@Param("id") Long id);

    WarehouseStock queryByWarehouseIdAndProductId(@Param("warehouseId") Long warehouseId,
                                                   @Param("productId") Long productId);

    void update(WarehouseStock stock);
}
