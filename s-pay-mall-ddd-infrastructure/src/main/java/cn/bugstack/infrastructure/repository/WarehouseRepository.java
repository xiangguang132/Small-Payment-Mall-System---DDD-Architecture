package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.warehouse.model.aggregate.WarehouseAggregate;
import cn.bugstack.domain.warehouse.repository.IWarehouseRepository;
import cn.bugstack.infrastructure.adapter.repository.AbstractRepository;
import cn.bugstack.infrastructure.dao.IWarehouseDao;
import cn.bugstack.infrastructure.dao.po.warehouse.Warehouse;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class WarehouseRepository extends AbstractRepository implements IWarehouseRepository {

    @Resource
    private IWarehouseDao warehouseDao;

    @Override
    public Long save(WarehouseAggregate warehouseAggregate) {
        if (warehouseAggregate == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "仓库信息不能为空");
        }
        Warehouse warehouse = Warehouse.builder()
                .warehouseCode(warehouseAggregate.getWarehouseCode())
                .name(warehouseAggregate.getName())
                .type(warehouseAggregate.getType())
                .address(warehouseAggregate.getAddress())
                .contactName(warehouseAggregate.getContactName())
                .contactPhone(warehouseAggregate.getContactPhone())
                .status(warehouseAggregate.getStatus())
                .isDel(warehouseAggregate.getIsDel())
                .createTime(warehouseAggregate.getCreateTime())
                .updateTime(warehouseAggregate.getUpdateTime())
                .build();
        warehouseDao.insert(warehouse);
        return warehouse.getId();
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "仓库id不能为空");
        }
        warehouseDao.deleteById(id);
        redisService.remove(cacheKeyById(id));
    }

    @Override
    public WarehouseAggregate queryById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "仓库id不能为空");
        }
        return getFromCacheOrDb(
                cacheKeyById(id),
                () -> {
                    Warehouse warehouse = warehouseDao.queryById(id);
                    if (warehouse == null) {
                        return null;
                    }
                    return WarehouseAggregate.builder()
                            .id(warehouse.getId())
                            .warehouseCode(warehouse.getWarehouseCode())
                            .name(warehouse.getName())
                            .type(warehouse.getType())
                            .address(warehouse.getAddress())
                            .contactName(warehouse.getContactName())
                            .contactPhone(warehouse.getContactPhone())
                            .status(warehouse.getStatus())
                            .isDel(warehouse.getIsDel())
                            .createTime(warehouse.getCreateTime())
                            .updateTime(warehouse.getUpdateTime())
                            .build();
                }
        );
    }

    @Override
    public void updateById(WarehouseAggregate warehouseAggregate) {
        if (warehouseAggregate == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "仓库信息不能为空");
        }
        if (warehouseAggregate.getId() == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "仓库id不能为空");
        }
        Warehouse warehouse = Warehouse.builder()
                .id(warehouseAggregate.getId())
                .warehouseCode(warehouseAggregate.getWarehouseCode())
                .name(warehouseAggregate.getName())
                .type(warehouseAggregate.getType())
                .address(warehouseAggregate.getAddress())
                .contactName(warehouseAggregate.getContactName())
                .contactPhone(warehouseAggregate.getContactPhone())
                .status(warehouseAggregate.getStatus())
                .isDel(warehouseAggregate.getIsDel())
                .createTime(warehouseAggregate.getCreateTime())
                .updateTime(warehouseAggregate.getUpdateTime())
                .build();
        warehouseDao.update(warehouse);
        redisService.remove(cacheKeyById(warehouseAggregate.getId()));
    }

    private String cacheKeyById(Long id) {
        return "s-pay-mall:warehouse:id:" + id;
    }
}
