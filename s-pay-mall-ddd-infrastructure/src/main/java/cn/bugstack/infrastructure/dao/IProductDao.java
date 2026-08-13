package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.product.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IProductDao {

    void insert(Product product);

    void deleteById(@Param("id") Long id);

    Product queryById(@Param("id") Long id);

    void update(Product product);

    long countByCategoryId(@Param("categoryId") Long categoryId);
}
