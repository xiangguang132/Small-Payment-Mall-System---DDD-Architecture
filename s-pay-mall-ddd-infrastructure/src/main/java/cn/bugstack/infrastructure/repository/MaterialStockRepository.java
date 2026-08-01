package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;
import cn.bugstack.domain.materialstock.repository.IMaterialStockRepository;
import cn.bugstack.infrastructure.dao.IMaterialStockDao;
import cn.bugstack.infrastructure.dao.po.MaterialStock;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MaterialStockRepository implements IMaterialStockRepository {

    @Resource
    private IMaterialStockDao  materialStockDao;

    @Override
    public MaterialStockAggregate queryById(Long id) {
        if (id == null) return null;
        MaterialStock materialStock = materialStockDao.queryById(id);
        if (materialStock == null) return null;
        return toAggregate(materialStock);
    }

    @Override
    public void inbound(Long materialId, String storageAddress, BigDecimal inboundQty) {
        MaterialStock current = materialStockDao.queryByMaterialIdAndStorageAddress(materialId, storageAddress);
        if (current == null) {
            MaterialStock stock = new MaterialStock();
            stock.setMaterialId(materialId);
            stock.setStorageAddress(storageAddress);
            stock.setAvailableQty(inboundQty);
            stock.setLockedQty(BigDecimal.ZERO);
            stock.setTotalQty(inboundQty);
            stock.setIsDel(0);
            stock.setCreateTime(LocalDateTime.now());
            stock.setUpdateTime(LocalDateTime.now());
            materialStockDao.insert(stock);
            return;
        }

        BigDecimal totalQty = current.getTotalQty() == null ? BigDecimal.ZERO : current.getTotalQty();
        BigDecimal lockedQty = current.getLockedQty() == null ? BigDecimal.ZERO : current.getLockedQty();
        BigDecimal nextTotalQty = totalQty.add(inboundQty);
        current.setTotalQty(nextTotalQty);
        current.setAvailableQty(nextTotalQty.subtract(lockedQty));
        current.setUpdateTime(LocalDateTime.now());
        materialStockDao.update(current);
    }

    @Override
    public void updateById(MaterialStockAggregate stock) {
        if (stock == null || stock.getId() == null) {
            throw new IllegalArgumentException("库存信息或库存id不能为空");
        }
        materialStockDao.update(toPo(stock));
    }

    @Override
    public boolean lockStock(Long stockId, BigDecimal lockQty) {
        if (stockId == null || lockQty == null || lockQty.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return materialStockDao.lockStock(stockId, lockQty) == 1;
    }

    @Override
    public boolean releaseStock(Long stockId, BigDecimal releaseQty) {
        if (stockId == null || releaseQty == null || releaseQty.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return materialStockDao.releaseStock(stockId, releaseQty) == 1;
    }

    @Override
    public boolean outboundLockedStock(Long stockId, BigDecimal outboundQty) {
        if (stockId == null || outboundQty == null || outboundQty.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return materialStockDao.outboundLockedStock(stockId, outboundQty) == 1;
    }

    @Override
    public boolean outboundAvailableStock(Long stockId, BigDecimal outboundQty) {
        if (stockId == null || outboundQty == null || outboundQty.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return materialStockDao.outboundAvailableStock(stockId, outboundQty) == 1;
    }

    @Override
    public List<MaterialStockAggregate> queryAvailableByMaterialId(Long materialId) {
        if (materialId == null) {
            return Collections.emptyList();
        }
        List<MaterialStock> stocks = materialStockDao.queryAvailableByMaterialId(materialId);
        if (stocks == null || stocks.isEmpty()) {
            return Collections.emptyList();
        }
        return stocks.stream()
                .map(this::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public List<MaterialStockAggregate> queryAvailableByMaterialIdExcludeStockId(Long materialId, Long excludeStockId) {
        if (materialId == null || excludeStockId == null) {
            return Collections.emptyList();
        }
        List<MaterialStock> stocks = materialStockDao.queryAvailableByMaterialIdExcludeStockId(materialId, excludeStockId);
        if (stocks == null || stocks.isEmpty()) {
            return Collections.emptyList();
        }
        return stocks.stream()
                .map(this::toAggregate)
                .collect(Collectors.toList());
    }

    private MaterialStock toPo(MaterialStockAggregate stock) {
        MaterialStock po = new MaterialStock();
        po.setId(stock.getId());
        po.setMaterialId(stock.getMaterialId());
        po.setStorageAddress(stock.getStorageAddress());
        po.setAvailableQty(stock.getAvailableQty());
        po.setLockedQty(stock.getLockedQty());
        po.setTotalQty(stock.getTotalQty());
        po.setIsDel(stock.getIsDel());
        po.setCreateTime(stock.getCreateTime());
        po.setUpdateTime(stock.getUpdateTime());
        return po;
    }

    private MaterialStockAggregate toAggregate(MaterialStock stock) {
        if (stock == null) {
            return null;
        }
        return MaterialStockAggregate.builder()
                .id(stock.getId())
                .materialId(stock.getMaterialId())
                .storageAddress(stock.getStorageAddress())
                .availableQty(stock.getAvailableQty())
                .lockedQty(stock.getLockedQty())
                .totalQty(stock.getTotalQty())
                .isDel(stock.getIsDel())
                .createTime(stock.getCreateTime())
                .updateTime(stock.getUpdateTime())
                .build();
    }


}
