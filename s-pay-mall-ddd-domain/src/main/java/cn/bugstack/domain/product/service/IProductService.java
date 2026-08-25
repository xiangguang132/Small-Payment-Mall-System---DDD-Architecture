package cn.bugstack.domain.product.service;

import cn.bugstack.domain.product.model.aggregate.ProductAggregate;

import java.util.List;

public interface IProductService {

    Long addNewProduct(ProductAggregate product);

    void deleteProductById(Long id);

    ProductAggregate queryProductById(Long id);

    void updateProductById(ProductAggregate updated);

    ProductAggregate onSale(Long id);

    long countProductPage(String name, String sku, Long categoryId, Integer status);

    List<ProductAggregate> queryProductPage(String name, String sku, Long categoryId, Integer status,
                                            Integer pageNo, Integer pageSize);

    long countProductSearch(String keyword);

    List<ProductAggregate> queryProductSearch(String keyword, Integer pageNo, Integer pageSize);
}
