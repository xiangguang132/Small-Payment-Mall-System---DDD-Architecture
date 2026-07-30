package cn.bugstack.domain.warehouse.repository;

import cn.bugstack.domain.warehouse.model.aggregate.WarehouseAggregate;

public interface IWarehouseRepository {

    Long save(WarehouseAggregate warehouseAggregate);

    void deleteById(Long id);

    WarehouseAggregate queryById(Long id);

    void updateById(WarehouseAggregate warehouseAggregate);
}
