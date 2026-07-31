package cn.bugstack.domain.materialstock.repository;

import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;

import java.math.BigDecimal;

public interface IMaterialStockRepository {

    MaterialStockAggregate queryById(Long id);

    void inbound(Long materialId, String storageAddress, BigDecimal inboundQty);

    void updateById(MaterialStockAggregate stock);

}
