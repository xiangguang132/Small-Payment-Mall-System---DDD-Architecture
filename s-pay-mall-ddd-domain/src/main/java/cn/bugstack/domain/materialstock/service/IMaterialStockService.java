package cn.bugstack.domain.materialstock.service;

import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;

import java.math.BigDecimal;

public interface IMaterialStockService {

    MaterialStockAggregate queryMaterialStockById(Long id);

    void inbound(Long materialId, String storageAddress, BigDecimal inboundQty, String reason);

    void lock(Long id, Integer quantity);
}
