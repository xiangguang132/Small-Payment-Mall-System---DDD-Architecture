package cn.bugstack.domain.materialstock.repository;

import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;

import java.math.BigDecimal;
import java.util.List;

public interface IMaterialStockRepository {

    MaterialStockAggregate queryById(Long id);

    void inbound(Long materialId, String storageAddress, BigDecimal inboundQty);

    void updateById(MaterialStockAggregate stock);

    boolean lockStock(Long stockId, BigDecimal lockQty);

    boolean releaseStock(Long stockId, BigDecimal releaseQty);

    boolean outboundLockedStock(Long stockId, BigDecimal outboundQty);

    boolean outboundAvailableStock(Long stockId, BigDecimal outboundQty);

    List<MaterialStockAggregate> queryAvailableByMaterialId(Long materialId);

    List<MaterialStockAggregate> queryAvailableByMaterialIdExcludeStockId(Long materialId, Long excludeStockId);
}
