package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.ProductType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IProductTypeDao {

    void insert(ProductType productType);

    void deleteById(@Param("id") Long id);

    ProductType queryById(Long id);
}
