package cn.bugstack.domain.producttype.repository;

import cn.bugstack.domain.producttype.model.aggregate.ProductTypeAggregate;

import java.util.List;

public interface IProductTypeRepository {

    Long save(ProductTypeAggregate productType);

    void deleteById(Long id);

    ProductTypeAggregate queryById(Long id);

    ProductTypeAggregate queryByTypeCode(String typeCode);

    void updateById(ProductTypeAggregate productType);

    long countProductByCategoryId(Long categoryId);

    long countByParentId(Long parentId);

    List<ProductTypeAggregate> queryValidList();
}
