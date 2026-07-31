package cn.bugstack.domain.materialstockallocation.service;

import cn.bugstack.domain.materialstockallocation.model.aggregate.MaterialStockAllocationAggregate;

public interface IMaterialStockAllocationService {

    String create(Long requestStockId, Integer quantity, String reason);

    MaterialStockAllocationAggregate queryByAllocationNo(String allocationNo);

    MaterialStockAllocationAggregate queryById(Long id);
}
