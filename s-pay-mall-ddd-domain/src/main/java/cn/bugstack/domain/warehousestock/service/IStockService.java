package cn.bugstack.domain.warehousestock.service;

import cn.bugstack.domain.warehousestock.model.aggregate.StockAggregate;

public interface IStockService {

    StockAggregate queryStockById(Long id);

    StockAggregate queryStockByWarehouseIdAndProductId(Long warehouseId, Long productId);

    Long addStock(Long warehouseId, Long productId);

    void adjustStock(Long warehouseId, Long productId, Integer quantity, String reason);

    void inbound(Long warehouseId, Long productId, Integer quantity);

    void outbound(Long warehouseId, Long productId, Integer quantity);
}
