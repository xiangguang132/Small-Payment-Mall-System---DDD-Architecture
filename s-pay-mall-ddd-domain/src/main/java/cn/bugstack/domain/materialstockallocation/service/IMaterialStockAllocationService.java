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

    void recordLockFailure(String allocationNo, String failReason, Integer maxRetryCount);

    List<MaterialStockAllocationAggregate> queryByStatus(Integer status, Integer pageNo, Integer pageSize);

    void lockWithAutoReleaseOnFailure(String allocationNo);
}
