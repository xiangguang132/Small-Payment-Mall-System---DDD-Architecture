package cn.bugstack.domain.materialstock.repository;

import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;

import java.math.BigDecimal;
import java.util.List;

public interface IMaterialStockRepository {

    MaterialStockAggregate queryById(Long id);

    void inbound(Long materialId, String storageAddress, BigDecimal inboundQty);

    void updateById(MaterialStockAggregate stock);

    List<MaterialStockAggregate> queryAvailableByMaterialIdExcludeStockId(Long materialId, Long excludeStockId);
}
