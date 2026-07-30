package cn.bugstack.domain.materialstock.repository;

import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;

import java.math.BigDecimal;

public interface IMaterialStockRepository {

    void inbound(Long materialId, String trim, BigDecimal inboundQty);

    void updateById(MaterialStockAggregate stock);
}
