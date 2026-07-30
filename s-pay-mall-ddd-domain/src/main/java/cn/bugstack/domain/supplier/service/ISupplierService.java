package cn.bugstack.domain.supplier.service;

import cn.bugstack.domain.supplier.model.aggregate.SupplierAggregate;

public interface ISupplierService {

    Long addNewSupplier(SupplierAggregate supplier);

    void deleteSupplierById(Long id);

    SupplierAggregate querySupplierById(Long id);

    void updateSupplierById(SupplierAggregate supplier);
}
