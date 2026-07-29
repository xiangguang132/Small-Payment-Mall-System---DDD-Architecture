package cn.bugstack.domain.producttype.service;

import cn.bugstack.domain.producttype.model.aggregate.ProductTypeAggregate;

public interface IProductTypeService {

    Long addNewProductType(ProductTypeAggregate productType);
    
    void deleteProductTypeById(Long id);

    ProductTypeAggregate queryProductTypeById(Long id);
}
