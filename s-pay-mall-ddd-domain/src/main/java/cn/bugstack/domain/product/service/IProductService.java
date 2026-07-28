package cn.bugstack.domain.product.service;

import cn.bugstack.domain.product.model.aggregate.ProductAggregate;

public interface IProductService {

    Long addNewProduct(ProductAggregate product);

    void deleteProductById(Long id);

    ProductAggregate updateProduct(ProductAggregate product);

    ProductAggregate queryProductById(Long id);


}
