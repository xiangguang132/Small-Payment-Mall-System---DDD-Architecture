package cn.bugstack.domain.materialstockallocation.service;

import cn.bugstack.domain.materialstockallocation.model.aggregate.MaterialStockAllocationAggregate;

import java.util.List;

public interface IMaterialStockAllocationService {

    String create(Long materialId, Integer quantity, String reason);

    MaterialStockAllocationAggregate queryByAllocationNo(String allocationNo);

    MaterialStockAllocationAggregate queryById(Long id);

    void lock(String allocationNo);

    void release(String allocationNo);

    void autoOutbound(String allocationNo);

    List<MaterialStockAllocationAggregate> queryByStatus(Integer status, Integer pageNo, Integer pageSize);
}
