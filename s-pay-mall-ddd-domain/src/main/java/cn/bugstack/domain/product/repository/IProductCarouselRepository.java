package cn.bugstack.domain.product.repository;

import cn.bugstack.domain.product.model.entity.ProductCarouselEntity;

import java.util.List;

public interface IProductCarouselRepository {

    Long save(ProductCarouselEntity entity);

    void deleteById(Long id);

    ProductCarouselEntity queryById(Long id);

    void updateById(ProductCarouselEntity entity);

    List<ProductCarouselEntity> queryActiveList();

    List<ProductCarouselEntity> queryPage(Integer pageNo, Integer pageSize);

    long countPage();
}
