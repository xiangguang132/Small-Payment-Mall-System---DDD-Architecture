package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.supplier.model.aggregate.SupplierAggregate;
import cn.bugstack.domain.supplier.repository.ISupplierRepository;
import cn.bugstack.infrastructure.dao.ISupplierDao;
import cn.bugstack.infrastructure.dao.po.Supplier;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class SupplierRepository implements ISupplierRepository {

    @Resource
    private ISupplierDao supplierDao;

    @Override
    public Long save(SupplierAggregate supplierAggregate) {
        if (supplierAggregate == null) {
            throw new IllegalArgumentException("供应商信息不能为空");
        }
        Supplier supplier = Supplier.builder()
                .supplierCode(supplierAggregate.getSupplierCode())
                .name(supplierAggregate.getName())
                .contactName(supplierAggregate.getContactName())
                .contactPhone(supplierAggregate.getContactPhone())
                .address(supplierAggregate.getAddress())
                .status(supplierAggregate.getStatus())
                .isDel(supplierAggregate.getIsDel())
                .createTime(supplierAggregate.getCreateTime())
                .updateTime(supplierAggregate.getUpdateTime())
                .build();
        supplierDao.insert(supplier);
        return supplier.getId();
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("供应商id不能为空");
        }
        supplierDao.deleteById(id);
    }

    @Override
    public SupplierAggregate queryById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("供应商id不能为空");
        }
        Supplier supplier = supplierDao.queryById(id);
        if (supplier == null) {
            return null;
        }
        return SupplierAggregate.builder()
                .id(supplier.getId())
                .supplierCode(supplier.getSupplierCode())
                .name(supplier.getName())
                .contactName(supplier.getContactName())
                .contactPhone(supplier.getContactPhone())
                .address(supplier.getAddress())
                .status(supplier.getStatus())
                .isDel(supplier.getIsDel())
                .createTime(supplier.getCreateTime())
                .updateTime(supplier.getUpdateTime())
                .build();
    }

    @Override
    public void updateById(SupplierAggregate supplierAggregate) {
        if (supplierAggregate == null) {
            throw new IllegalArgumentException("供应商信息不能为空");
        }
        if (supplierAggregate.getId() == null) {
            throw new IllegalArgumentException("供应商id不能为空");
        }
        Supplier supplier = Supplier.builder()
                .id(supplierAggregate.getId())
                .supplierCode(supplierAggregate.getSupplierCode())
                .name(supplierAggregate.getName())
                .contactName(supplierAggregate.getContactName())
                .contactPhone(supplierAggregate.getContactPhone())
                .address(supplierAggregate.getAddress())
                .status(supplierAggregate.getStatus())
                .isDel(supplierAggregate.getIsDel())
                .createTime(supplierAggregate.getCreateTime())
                .updateTime(supplierAggregate.getUpdateTime())
                .build();
        supplierDao.update(supplier);
    }
}
