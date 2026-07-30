package cn.bugstack.domain.warehouse.service;

import cn.bugstack.domain.warehouse.model.aggregate.WarehouseAggregate;

public interface IWarehouseService {

    Long addWarehouse(WarehouseAggregate warehouse);

    void deleteWarehouseById(Long id);

    WarehouseAggregate queryWarehouseById(Long id);

    void updateWarehouseById(WarehouseAggregate warehouse);
}
