package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.product.ProductCarousel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IProductCarouselDao {

    void insert(ProductCarousel productCarousel);

    void deleteById(@Param("id") Long id);

    ProductCarousel queryById(@Param("id") Long id);

    void update(ProductCarousel productCarousel);

    List<ProductCarousel> queryActiveList();

    List<ProductCarousel> queryPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    long countPage();
}
