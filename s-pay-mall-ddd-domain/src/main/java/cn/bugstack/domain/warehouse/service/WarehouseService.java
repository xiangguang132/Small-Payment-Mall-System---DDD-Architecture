package cn.bugstack.domain.warehouse.service;

import cn.bugstack.domain.warehouse.model.aggregate.WarehouseAggregate;
import cn.bugstack.domain.warehouse.model.vo.WarehouseStatusVO;
import cn.bugstack.domain.warehouse.model.vo.WarehouseTypeVO;
import cn.bugstack.domain.warehouse.repository.IWarehouseRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class WarehouseService implements IWarehouseService {

    @Resource
    private IWarehouseRepository warehouseRepository;

    @Override
    public Long addWarehouse(WarehouseAggregate warehouse) {
        if (warehouse == null) {
            throw new IllegalArgumentException("仓库信息不能为空");
        }
        validate(warehouse);
        return warehouseRepository.save(warehouse);
    }

    @Override
    public void deleteWarehouseById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("仓库id不能为空");
        }
        WarehouseAggregate current = warehouseRepository.queryById(id);
        if (current == null) {
            throw new IllegalArgumentException("仓库不存在");
        }
        warehouseRepository.deleteById(id);
    }

    @Override
    public WarehouseAggregate queryWarehouseById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("仓库id不能为空");
        }
        return warehouseRepository.queryById(id);
    }

    @Override
    public void updateWarehouseById(WarehouseAggregate warehouse) {
        if (warehouse == null) {
            throw new IllegalArgumentException("仓库信息不能为空");
        }
        if (warehouse.getId() == null) {
            throw new IllegalArgumentException("仓库id不能为空");
        }
        validate(warehouse);
        WarehouseAggregate current = warehouseRepository.queryById(warehouse.getId());
        if (current == null) {
            throw new IllegalArgumentException("仓库不存在");
        }
        warehouse.setCreateTime(current.getCreateTime());
        warehouse.setIsDel(current.getIsDel());
        warehouseRepository.updateById(warehouse);
    }

    private void validate(WarehouseAggregate warehouse) {
        if (!WarehouseTypeVO.isValid(warehouse.getType())) {
            throw new IllegalArgumentException("仓库类型值非法");
        }
        if (!WarehouseStatusVO.isValid(warehouse.getStatus())) {
            throw new IllegalArgumentException("仓库状态值非法");
        }
        if (warehouse.getWarehouseCode() == null || warehouse.getWarehouseCode().trim().isEmpty()) {
            throw new IllegalArgumentException("仓库编码不能为空");
        }
        if (warehouse.getName() == null || warehouse.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("仓库名称不能为空");
        }
    }
}
