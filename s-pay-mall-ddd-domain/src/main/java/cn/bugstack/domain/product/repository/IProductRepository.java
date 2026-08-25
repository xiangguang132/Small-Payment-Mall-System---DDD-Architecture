package cn.bugstack.domain.product.repository;

import cn.bugstack.domain.product.model.aggregate.ProductAggregate;

import java.util.List;

public interface IProductRepository {

    Long save(ProductAggregate productAggregate);

    void deleteById(Long id);

    ProductAggregate queryById(Long id);

    void updateById(ProductAggregate updated);

    long countByCategoryId(Long categoryId);

    long countPage(String name, String sku, Long categoryId, Integer status);

    List<ProductAggregate> queryPage(String name, String sku, Long categoryId, Integer status,
                                     Integer offset, Integer limit);

    long countByKeyword(String keyword);

    List<ProductAggregate> queryByKeyword(String keyword, Integer offset, Integer limit);
}
