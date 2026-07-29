package cn.bugstack.domain.product.repository;

import cn.bugstack.domain.product.model.aggregate.ProductAggregate;

public interface IProductRepository {

    Long save(ProductAggregate productAggregate);

    void deleteById(Long id);

    ProductAggregate queryById(Long id);
}
