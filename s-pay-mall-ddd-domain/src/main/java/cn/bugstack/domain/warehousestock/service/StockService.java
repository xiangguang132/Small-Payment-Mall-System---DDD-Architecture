package cn.bugstack.domain.warehousestock.service;

import cn.bugstack.domain.warehousestock.model.aggregate.StockAggregate;
import cn.bugstack.domain.warehousestock.repository.IStockRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class StockService implements IStockService {

    @Resource
    private IStockRepository  stockRepository;

    @Override
    public StockAggregate queryStockById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        return stockRepository.queryById(id);
    }

    @Override
    public StockAggregate queryStockByWarehouseIdAndProductId(Long warehouseId, Long productId) {
        if (warehouseId == null) {
            throw new IllegalArgumentException("仓库id不能为空");
        }
        if (productId == null) {
            throw new IllegalArgumentException("商品id不能为空");
        }
        return stockRepository.queryByWarehouseIdAndProductId(warehouseId, productId);
    }

    @Override
    public Long addStock(Long warehouseId, Long productId) {
        if (warehouseId == null) {
            throw new IllegalArgumentException("仓库id不能为空");
        }
        if (productId == null) {
            throw new IllegalArgumentException("商品id不能为空");
        }

        StockAggregate current = stockRepository.queryByWarehouseIdAndProductId(warehouseId, productId);
        if (current != null) {
            throw new IllegalArgumentException("库存记录已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        StockAggregate stock = StockAggregate.create(warehouseId, productId);
        stock.setCreateTime(now);
        stock.setUpdateTime(now);
        return stockRepository.save(stock);
    }

    @Override
    public void adjustStock(Long warehouseId, Long productId, Integer quantity, String reason) {
        if (quantity == null || quantity == 0) {
            throw new IllegalArgumentException("调整数量不能为空且不能为0");
        }
        applyDelta(warehouseId, productId, BigDecimal.valueOf(quantity), reason);
    }

    @Override
    public void inbound(Long warehouseId, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("入库数量必须大于0");
        }
        applyDelta(warehouseId, productId, BigDecimal.valueOf(quantity), "入库");
    }

    @Override
    public void outbound(Long warehouseId, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("出库数量必须大于0");
        }
        applyDelta(warehouseId, productId, BigDecimal.valueOf(quantity).negate(), "出库");
    }

    private void applyDelta(Long warehouseId, Long productId, BigDecimal delta, String reason) {
        if (warehouseId == null) {
            throw new IllegalArgumentException("仓库id不能为空");
        }
        if (productId == null) {
            throw new IllegalArgumentException("商品id不能为空");
        }

        StockAggregate current = stockRepository.queryByWarehouseIdAndProductId(warehouseId, productId);
        if (current == null) {
            current = StockAggregate.create(warehouseId, productId);
            current.setCreateTime(LocalDateTime.now());
        }

        BigDecimal totalQty = current.getTotalQty() == null ? BigDecimal.ZERO : current.getTotalQty();
        BigDecimal lockedQty = current.getLockedQty() == null ? BigDecimal.ZERO : current.getLockedQty();
        BigDecimal nextQty = totalQty.add(delta);
        if (nextQty.compareTo(lockedQty) < 0) {
            throw new IllegalArgumentException("库存不能小于锁定库存");
        }

        current.setTotalQty(nextQty);
        current.setAvailableQty(nextQty.subtract(lockedQty));
        current.setUpdateTime(LocalDateTime.now());

        if (current.getId() == null) {
            stockRepository.save(current);
        } else {
            stockRepository.updateById(current);
        }
    }
}
