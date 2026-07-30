package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.suppliermaterial.model.aggregate.SupplierMaterialAggregate;
import cn.bugstack.domain.suppliermaterial.repository.ISupplierMaterialRepository;
import cn.bugstack.infrastructure.dao.ISupplierMaterialDao;
import cn.bugstack.infrastructure.dao.po.SupplierMaterial;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class SupplierMaterialRepository implements ISupplierMaterialRepository {

    @Resource
    private ISupplierMaterialDao supplierMaterialDao;

    @Override
    public SupplierMaterialAggregate queryById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("供应商原料关系id不能为空");
        }
        return toAggregate(supplierMaterialDao.queryById(id));
    }

    private SupplierMaterialAggregate toAggregate(SupplierMaterial supplierMaterial) {
        if (supplierMaterial == null) {
            return null;
        }
        return SupplierMaterialAggregate.builder()
                .id(supplierMaterial.getId())
                .supplierId(supplierMaterial.getSupplierId())
                .materialId(supplierMaterial.getMaterialId())
                .supplyPrice(supplierMaterial.getSupplyPrice())
                .leadTimeDays(supplierMaterial.getLeadTimeDays())
                .status(supplierMaterial.getStatus())
                .isDel(supplierMaterial.getIsDel())
                .createTime(supplierMaterial.getCreateTime())
                .updateTime(supplierMaterial.getUpdateTime())
                .build();
    }
}
