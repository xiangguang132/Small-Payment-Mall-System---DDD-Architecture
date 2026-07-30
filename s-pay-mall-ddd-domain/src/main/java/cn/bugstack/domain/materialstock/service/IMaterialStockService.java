package cn.bugstack.domain.materialstock.service;

import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;

public interface IMaterialStockService {

    MaterialStockAggregate queryStockById(Long id);

    MaterialStockAggregate queryStockByMaterialIdAndStorageAddress(Long materialId, String storageAddress);

    Long addStock(Long materialId, String storageAddress);

    void adjustStock(Long id, Integer quantity, String reason);

    void inbound(Long supplierMaterialId, String storageAddress, Integer quantity);

    void productionOutbound(Long id, Integer quantity);

    void lock(Long id, Integer quantity);

    void release(Long id, Integer quantity);

    void confirmOutbound(Long id, Integer quantity);
}
