package cn.bugstack.domain.materialstock.service;

import cn.bugstack.domain.material.service.IMaterialService;
import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;
import cn.bugstack.domain.materialstock.repository.IMaterialStockRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class MaterialStockService implements IMaterialStockService {

    @Resource
    private IMaterialStockRepository materialStockRepository;
    @Resource
    private IMaterialService materialService;

    @Override
    public MaterialStockAggregate queryMaterialStockById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("原料库存的ID不能为空");
        }
        MaterialStockAggregate materialStock = materialStockRepository.queryById(id);
        if  (materialStock == null) {
            throw new IllegalArgumentException("原料库存的不存在");
        }
        return materialStock;
    }

    @Override
    public void inbound(Long materialId, String storageAddress, BigDecimal inboundQty, String reason) {
        if (materialId == null) {
            throw new IllegalArgumentException("原料ID不能为空");
        }
        if (storageAddress == null || storageAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("存储位置不能为空");
        }
        if (inboundQty == null || inboundQty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("入库数量必须大于0");
        }
        materialService.validateMaterialEnabled(materialId);
        materialStockRepository.inbound(materialId, storageAddress.trim(), inboundQty);
    }

    @Override
    public MaterialStockAggregate adjust(Long id, Long materialId, String storageAddress, BigDecimal quantity, String reason) {
        if (materialId == null) {
            throw new IllegalArgumentException("原料ID不能为空");
        }
        if (storageAddress == null || storageAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("存储位置不能为空");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("调整后库存数量不能为空且不能小于0");
        }
        materialService.validateMaterialEnabled(materialId);
        MaterialStockAggregate stock = getExistingStockById(id);
        BigDecimal lockedQty = valueOf(stock.getLockedQty());
        if (quantity.compareTo(lockedQty) < 0) {
            throw new IllegalArgumentException("调整后库存不能小于锁定库存");
        }
        stock.setMaterialId(materialId);
        stock.setStorageAddress(storageAddress.trim());
        stock.setTotalQty(quantity);
        stock.setAvailableQty(quantity.subtract(lockedQty));
        saveUpdated(stock);
        return stock;
    }

    @Override
    public MaterialStockAggregate manualOutbound(Long id, Integer quantity, String reason) {
        validatePositiveQuantity(quantity, "出库数量必须大于0");
        MaterialStockAggregate stock = getExistingStockById(id);
        BigDecimal outboundQty = BigDecimal.valueOf(quantity);
        BigDecimal available = valueOf(stock.getAvailableQty());
        if (available.compareTo(outboundQty) < 0) {
            throw new IllegalArgumentException("原料可用库存不足，不能出库");
        }
        stock.setAvailableQty(available.subtract(outboundQty));
        stock.setTotalQty(valueOf(stock.getTotalQty()).subtract(outboundQty));
        saveUpdated(stock);
        return stock;
    }

    @Override
    public void lock(Long id, Integer quantity) {
        validatePositiveQuantity(quantity, "锁定数量必须大于0");
        MaterialStockAggregate stock = getExistingStockById(id);
        // 从 stock 对象中获取“可用库存”数量
        BigDecimal available = valueOf(stock.getAvailableQty());
        BigDecimal locked = BigDecimal.valueOf(quantity);
        if (available.compareTo(locked) < 0) {
            throw new IllegalArgumentException("原料可用库存不足，不能锁定");
        }
        stock.setAvailableQty(available.subtract(locked));
        stock.setLockedQty(valueOf(stock.getLockedQty()).add(locked));
        saveUpdated(stock);
    }

    private void validatePositiveQuantity(Integer quantity, String message) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private MaterialStockAggregate getExistingStockById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("库存id不能为空");
        }
        MaterialStockAggregate stock = materialStockRepository.queryById(id);
        if (stock == null) {
            throw new IllegalArgumentException("原料库存记录不存在");
        }
        return stock;
    }

    private BigDecimal valueOf(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void saveUpdated(MaterialStockAggregate stock) {
        stock.setUpdateTime(LocalDateTime.now());
        materialStockRepository.updateById(stock);
    }
}
