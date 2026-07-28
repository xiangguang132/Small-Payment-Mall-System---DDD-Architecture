package cn.bugstack.domain.product.repository;

import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import java.util.List;

public interface IProductRepository {

    Long save(ProductAggregate productAggregate);

    void deleteById(Long id);

    ProductAggregate queryById(Long id);

//    void update(ProductAggregate productAggregate);
//
//    ProductAggregate queryById(Long id);
//
//    List<ProductAggregate> queryPage(Integer pageNo, Integer pageSize);
//
//    Long queryCount();
}
