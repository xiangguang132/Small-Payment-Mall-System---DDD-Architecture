package cn.bugstack.domain.materialstockallocation.service;

public interface IMaterialStockAllocationService {

    String create(Long requestStockId, Integer quantity, String reason);
}
