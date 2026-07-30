package cn.bugstack.domain.materialstock.service;

import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;
import cn.bugstack.domain.materialstock.repository.IMaterialStockRepository;
import cn.bugstack.domain.suppliermaterial.model.aggregate.SupplierMaterialAggregate;
import cn.bugstack.domain.suppliermaterial.repository.ISupplierMaterialRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class MaterialStockService implements IMaterialStockService {

    @Resource
    private IMaterialStockRepository materialStockRepository;
    @Resource
    private ISupplierMaterialRepository supplierMaterialRepository;

    @Override
    public MaterialStockAggregate queryStockById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("库存id不能为空");
        }
        return materialStockRepository.queryById(id);
    }

    @Override
    public MaterialStockAggregate queryStockByMaterialIdAndStorageAddress(Long materialId, String storageAddress) {
        validateMaterialId(materialId);
        return materialStockRepository.queryByMaterialIdAndStorageAddress(materialId, normalizeStorageAddress(storageAddress));
    }

    @Override
    public Long addStock(Long materialId, String storageAddress) {
        validateMaterialId(materialId);
        String normalizedStorageAddress = normalizeStorageAddress(storageAddress);
        MaterialStockAggregate current =
                materialStockRepository.queryByMaterialIdAndStorageAddress(materialId, normalizedStorageAddress);
        if (current != null) {
            throw new IllegalArgumentException("原料库存记录已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        MaterialStockAggregate stock = MaterialStockAggregate.create(materialId, normalizedStorageAddress);
        stock.setCreateTime(now);
        stock.setUpdateTime(now);
        return materialStockRepository.save(stock);
    }

    @Override
    public void adjustStock(Long id, Integer quantity, String reason) {
        if (quantity == null || quantity == 0) {
            throw new IllegalArgumentException("调整数量不能为空且不能为0");
        }
        MaterialStockAggregate stock = getExistingStockById(id);
        applyDelta(stock, BigDecimal.valueOf(quantity), reason);
    }

    @Override
    public void inbound(Long supplierMaterialId, String storageAddress, Integer quantity) {
        validatePositiveQuantity(quantity, "入库数量必须大于0");
        SupplierMaterialAggregate supplierMaterial = getEnabledSupplierMaterial(supplierMaterialId);
        applyDelta(supplierMaterial.getMaterialId(), normalizeStorageAddress(storageAddress),
                BigDecimal.valueOf(quantity), "原料进货入库");
    }

    @Override
    public void productionOutbound(Long id, Integer quantity) {
        validatePositiveQuantity(quantity, "生产领料数量必须大于0");
        MaterialStockAggregate stock = getExistingStockById(id);
        BigDecimal available = valueOf(stock.getAvailableQty());
        BigDecimal outbound = BigDecimal.valueOf(quantity);
        if (available.compareTo(outbound) < 0) {
            throw new IllegalArgumentException("原料可用库存不足");
        }
        stock.setAvailableQty(available.subtract(outbound));
        stock.setTotalQty(valueOf(stock.getTotalQty()).subtract(outbound));
        saveUpdated(stock);
    }

    @Override
    public void lock(Long id, Integer quantity) {
        validatePositiveQuantity(quantity, "锁定数量必须大于0");
        MaterialStockAggregate stock = getExistingStockById(id);
        BigDecimal available = valueOf(stock.getAvailableQty());
        BigDecimal locked = BigDecimal.valueOf(quantity);
        if (available.compareTo(locked) < 0) {
            throw new IllegalArgumentException("原料可用库存不足，不能锁定");
        }
        stock.setAvailableQty(available.subtract(locked));
        stock.setLockedQty(valueOf(stock.getLockedQty()).add(locked));
        saveUpdated(stock);
    }

    @Override
    public void release(Long id, Integer quantity) {
        validatePositiveQuantity(quantity, "释放数量必须大于0");
        MaterialStockAggregate stock = getExistingStockById(id);
        BigDecimal locked = valueOf(stock.getLockedQty());
        BigDecimal release = BigDecimal.valueOf(quantity);
        if (locked.compareTo(release) < 0) {
            throw new IllegalArgumentException("锁定库存不足，不能释放");
        }
        stock.setLockedQty(locked.subtract(release));
        stock.setAvailableQty(valueOf(stock.getAvailableQty()).add(release));
        saveUpdated(stock);
    }

    @Override
    public void confirmOutbound(Long id, Integer quantity) {
        validatePositiveQuantity(quantity, "确认出库数量必须大于0");
        MaterialStockAggregate stock = getExistingStockById(id);
        BigDecimal locked = valueOf(stock.getLockedQty());
        BigDecimal outbound = BigDecimal.valueOf(quantity);
        if (locked.compareTo(outbound) < 0) {
            throw new IllegalArgumentException("锁定库存不足，不能确认出库");
        }
        stock.setLockedQty(locked.subtract(outbound));
        stock.setTotalQty(valueOf(stock.getTotalQty()).subtract(outbound));
        saveUpdated(stock);
    }

    private void applyDelta(Long materialId, String storageAddress, BigDecimal delta, String reason) {
        validateMaterialId(materialId);
        String normalizedStorageAddress = normalizeStorageAddress(storageAddress);
        MaterialStockAggregate stock =
                materialStockRepository.queryByMaterialIdAndStorageAddress(materialId, normalizedStorageAddress);
        if (stock == null) {
            stock = MaterialStockAggregate.create(materialId, normalizedStorageAddress);
            stock.setCreateTime(LocalDateTime.now());
        }

        applyDelta(stock, delta, reason);
    }

    private void applyDelta(MaterialStockAggregate stock, BigDecimal delta, String reason) {
        BigDecimal total = valueOf(stock.getTotalQty()).add(delta);
        BigDecimal locked = valueOf(stock.getLockedQty());
        if (total.compareTo(locked) < 0) {
            throw new IllegalArgumentException("库存不能小于锁定库存");
        }
        stock.setTotalQty(total);
        stock.setAvailableQty(total.subtract(locked));
        save(stock);
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

    private SupplierMaterialAggregate getEnabledSupplierMaterial(Long supplierMaterialId) {
        if (supplierMaterialId == null) {
            throw new IllegalArgumentException("供应商原料关系id不能为空");
        }
        SupplierMaterialAggregate supplierMaterial = supplierMaterialRepository.queryById(supplierMaterialId);
        if (supplierMaterial == null) {
            throw new IllegalArgumentException("供应商原料关系不存在");
        }
        if (!supplierMaterial.enabled()) {
            throw new IllegalArgumentException("供应商原料关系未启用");
        }
        if (supplierMaterial.getMaterialId() == null) {
            throw new IllegalArgumentException("供应商原料关系未绑定原料");
        }
        return supplierMaterial;
    }

    private void save(MaterialStockAggregate stock) {
        stock.setUpdateTime(LocalDateTime.now());
        if (stock.getId() == null) {
            materialStockRepository.save(stock);
        } else {
            materialStockRepository.updateById(stock);
        }
    }

    private void saveUpdated(MaterialStockAggregate stock) {
        stock.setUpdateTime(LocalDateTime.now());
        materialStockRepository.updateById(stock);
    }

    private void validateMaterialId(Long materialId) {
        if (materialId == null) {
            throw new IllegalArgumentException("原料id不能为空");
        }
    }

    private void validatePositiveQuantity(Integer quantity, String message) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private String normalizeStorageAddress(String storageAddress) {
        if (storageAddress == null || storageAddress.trim().isEmpty()) {
            return "默认库位";
        }
        return storageAddress.trim();
    }

    private BigDecimal valueOf(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
