package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.warehousestock.model.aggregate.StockAggregate;
import cn.bugstack.domain.warehousestock.repository.IStockRepository;
import cn.bugstack.infrastructure.dao.IWarehouseStockDao;
import cn.bugstack.infrastructure.dao.po.WarehouseStock;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class WarehouseStockRepository implements IStockRepository {

    @Resource
    private IWarehouseStockDao warehouseStockDao;

    @Override
    public StockAggregate queryById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "库存id不能为空");
        }
        WarehouseStock stock = warehouseStockDao.queryById(id);
        return toAggregate(stock);
    }

    @Override
    public StockAggregate queryByWarehouseIdAndProductId(Long warehouseId, Long productId) {
        if (warehouseId == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "仓库id不能为空");
        }
        if (productId == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品id不能为空");
        }
        WarehouseStock stock = warehouseStockDao.queryByWarehouseIdAndProductId(warehouseId, productId);
        return toAggregate(stock);
    }

    @Override
    public Long save(StockAggregate stock) {
        if (stock == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "库存信息不能为空");
        }
        WarehouseStock po = toPo(stock);
        warehouseStockDao.insert(po);
        return po.getId();
    }

    @Override
    public void updateById(StockAggregate stock) {
        if (stock == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "库存信息不能为空");
        }
        if (stock.getId() == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "库存id不能为空");
        }
        WarehouseStock po = toPo(stock);
        warehouseStockDao.update(po);
    }

    private StockAggregate toAggregate(WarehouseStock stock) {
        if (stock == null) {
            return null;
        }
        return StockAggregate.builder()
                .id(stock.getId())
                .warehouseId(stock.getWarehouseId())
                .productId(stock.getProductId())
                .availableQty(stock.getAvailableQty())
                .lockedQty(stock.getLockedQty())
                .totalQty(stock.getTotalQty())
                .isDel(stock.getIsDel())
                .createTime(stock.getCreateTime())
                .updateTime(stock.getUpdateTime())
                .build();
    }

    private WarehouseStock toPo(StockAggregate stock) {
        WarehouseStock po = new WarehouseStock();
        po.setId(stock.getId());
        po.setWarehouseId(stock.getWarehouseId());
        po.setProductId(stock.getProductId());
        po.setAvailableQty(stock.getAvailableQty());
        po.setLockedQty(stock.getLockedQty());
        po.setTotalQty(stock.getTotalQty());
        po.setIsDel(stock.getIsDel());
        po.setCreateTime(stock.getCreateTime());
        po.setUpdateTime(stock.getUpdateTime());
        return po;
    }
}
