package cn.bugstack.domain.producttype.service;

import cn.bugstack.domain.producttype.model.aggregate.ProductTypeAggregate;

import java.util.List;

public interface IProductTypeService {

    Long addNewProductType(ProductTypeAggregate productType);
    
    void deleteProductTypeById(Long id);

    ProductTypeAggregate queryProductTypeById(Long id);

    ProductTypeAggregate onSale(Long id);

    void updateProductTypeById(ProductTypeAggregate updated);

    List<ProductTypeAggregate> queryValidProductTypes();
}
