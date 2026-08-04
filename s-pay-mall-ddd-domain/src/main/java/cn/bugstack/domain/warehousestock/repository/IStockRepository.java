package cn.bugstack.domain.warehousestock.repository;

import cn.bugstack.domain.warehousestock.model.aggregate.StockAggregate;

import java.math.BigDecimal;

public interface IStockRepository {

    StockAggregate queryById(Long id);

    StockAggregate queryByWarehouseIdAndProductId(Long warehouseId, Long productId);

    Long save(StockAggregate stock);

    void updateById(StockAggregate stock);

    boolean saveFlow(Long warehouseId,
                     Long productId,
                     BigDecimal quantity,
                     String bizType,
                     String bizNo,
                     String reason);
}
