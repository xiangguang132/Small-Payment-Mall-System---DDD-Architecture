package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.product.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IProductDao {

    void insert(Product product);

    void deleteById(@Param("id") Long id);

    Product queryById(@Param("id") Long id);

    void update(Product product);

    long countByCategoryId(@Param("categoryId") Long categoryId);

    long countPage(@Param("name") String name,
                   @Param("sku") String sku,
                   @Param("categoryId") Long categoryId,
                   @Param("status") Integer status);

    List<Product> queryPage(@Param("name") String name,
                            @Param("sku") String sku,
                            @Param("categoryId") Long categoryId,
                            @Param("status") Integer status,
                            @Param("offset") Integer offset,
                            @Param("limit") Integer limit);

    long countSearch(@Param("keyword") String keyword);

    List<Product> querySearch(@Param("keyword") String keyword,
                              @Param("offset") Integer offset,
                              @Param("limit") Integer limit);
}
