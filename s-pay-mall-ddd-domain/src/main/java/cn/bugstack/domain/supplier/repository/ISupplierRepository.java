package cn.bugstack.domain.supplier.repository;

import cn.bugstack.domain.supplier.model.aggregate.SupplierAggregate;

public interface ISupplierRepository {

    Long save(SupplierAggregate supplierAggregate);

    void deleteById(Long id);

    SupplierAggregate queryById(Long id);

    void updateById(SupplierAggregate supplierAggregate);
}
