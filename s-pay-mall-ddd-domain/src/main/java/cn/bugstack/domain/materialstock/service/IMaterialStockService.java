package cn.bugstack.domain.materialstock.service;

import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;

import java.math.BigDecimal;

public interface IMaterialStockService {

    MaterialStockAggregate queryMaterialStockById(Long id);

    void inbound(Long materialId, String storageAddress, BigDecimal inboundQty, String reason);

    MaterialStockAggregate adjust(Long id, Long materialId, String storageAddress, BigDecimal quantity, String reason);

    void lock(Long id, Integer quantity);

    MaterialStockAggregate manualOutbound(Long id, Integer quantity, String reason);

    MaterialStockAggregate autoOutbound(Long id, Integer quantity, String reason);

    void release(Long id, Integer quantity);
}
