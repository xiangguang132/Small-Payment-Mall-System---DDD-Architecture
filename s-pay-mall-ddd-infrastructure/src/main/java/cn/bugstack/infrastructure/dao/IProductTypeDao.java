package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.product.ProductType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IProductTypeDao {

    void insert(ProductType productType);

    void deleteById(@Param("id") Long id);

    ProductType queryById(Long id);

    ProductType queryByTypeCode(@Param("typeCode") String typeCode);

    void update(ProductType productType);

    long countByParentId(@Param("parentId") Long parentId);

    List<ProductType> queryValidList();
}
