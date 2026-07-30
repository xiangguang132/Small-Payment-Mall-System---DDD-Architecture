package cn.bugstack.domain.suppliermaterial.repository;

import cn.bugstack.domain.suppliermaterial.model.aggregate.SupplierMaterialAggregate;

public interface ISupplierMaterialRepository {

    SupplierMaterialAggregate queryById(Long id);
}
