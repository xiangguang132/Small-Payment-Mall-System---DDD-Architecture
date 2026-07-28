package cn.bugstack.domain.product.service;

import cn.bugstack.domain.product.model.aggregate.ProductAggregate;

public interface IProductService {

    Long addNewProduct(ProductAggregate product);

    ProductAggregate updateProduct(ProductAggregate product);

    void deleteProductById(Long id);

    ProductAggregate queryProductById(Long id);


}
