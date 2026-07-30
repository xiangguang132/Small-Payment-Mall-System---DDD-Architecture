package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;
import cn.bugstack.domain.materialstock.repository.IMaterialStockRepository;
import cn.bugstack.infrastructure.dao.IMaterialStockDao;
import cn.bugstack.infrastructure.dao.po.MaterialStock;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class MaterialStockRepository implements IMaterialStockRepository {

    @Resource
    private IMaterialStockDao materialStockDao;

    @Override
    public MaterialStockAggregate queryById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("库存id不能为空");
        }
        return toAggregate(materialStockDao.queryById(id));
    }

    @Override
    public MaterialStockAggregate queryByMaterialIdAndStorageAddress(Long materialId, String storageAddress) {
        if (materialId == null) {
            throw new IllegalArgumentException("原料id不能为空");
        }
        return toAggregate(materialStockDao.queryByMaterialIdAndStorageAddress(materialId, storageAddress));
    }

    @Override
    public Long save(MaterialStockAggregate stock) {
        if (stock == null) {
            throw new IllegalArgumentException("库存信息不能为空");
        }
        MaterialStock po = toPo(stock);
        materialStockDao.insert(po);
        return po.getId();
    }

    @Override
    public void updateById(MaterialStockAggregate stock) {
        if (stock == null || stock.getId() == null) {
            throw new IllegalArgumentException("库存信息或库存id不能为空");
        }
        materialStockDao.update(toPo(stock));
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
}
