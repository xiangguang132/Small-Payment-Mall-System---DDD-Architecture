package cn.bugstack.domain.producttype.repository;

import cn.bugstack.domain.producttype.model.aggregate.ProductTypeAggregate;

public interface IProductTypeRepository {

    Long save(ProductTypeAggregate productType);

    void deleteById(Long id);
}
