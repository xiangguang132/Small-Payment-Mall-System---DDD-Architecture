package cn.bugstack.domain.supplier.service;

import cn.bugstack.domain.supplier.model.aggregate.SupplierAggregate;
import cn.bugstack.domain.supplier.model.vo.SupplierStatusVO;
import cn.bugstack.domain.supplier.repository.ISupplierRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class SupplierService implements ISupplierService {

    @Resource
    private ISupplierRepository supplierRepository;

    @Override
    public Long addNewSupplier(SupplierAggregate supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("供应商信息不能为空");
        }
        validate(supplier);
        return supplierRepository.save(supplier);
    }

    @Override
    public void deleteSupplierById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("供应商id不能为空");
        }
        SupplierAggregate current = supplierRepository.queryById(id);
        if (current == null) {
            throw new IllegalArgumentException("供应商不存在");
        }
        supplierRepository.deleteById(id);
    }

    @Override
    public SupplierAggregate querySupplierById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("供应商id不能为空");
        }
        return supplierRepository.queryById(id);
    }

    @Override
    public void updateSupplierById(SupplierAggregate supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("供应商信息不能为空");
        }
        if (supplier.getId() == null) {
            throw new IllegalArgumentException("供应商id不能为空");
        }
        validate(supplier);

        SupplierAggregate current = supplierRepository.queryById(supplier.getId());
        if (current == null) {
            throw new IllegalArgumentException("供应商不存在");
        }

        supplier.setCreateTime(current.getCreateTime());
        supplier.setIsDel(current.getIsDel());
        supplier.setUpdateTime(LocalDateTime.now());
        supplierRepository.updateById(supplier);
    }

    private void validate(SupplierAggregate supplier) {
        if (!SupplierStatusVO.isValid(supplier.getStatus())) {
            throw new IllegalArgumentException("供应商状态值非法");
        }
        if (supplier.getSupplierCode() == null || supplier.getSupplierCode().trim().isEmpty()) {
            throw new IllegalArgumentException("供应商编码不能为空");
        }
        if (supplier.getName() == null || supplier.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("供应商名称不能为空");
        }
    }
}
