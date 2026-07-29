package cn.bugstack.infrastructure.repository;


import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import cn.bugstack.domain.product.repository.IProductRepository;
import cn.bugstack.infrastructure.dao.IProductDao;
import cn.bugstack.infrastructure.dao.po.Product;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class ProductRepository implements IProductRepository {

    @Resource
    private IProductDao productDao;

    @Override
    public Long save(ProductAggregate productAggregate) {
        if (productAggregate == null) {
            throw new IllegalArgumentException("商品信息不能为空!");
        }
        Product product = Product.builder()
                .name(productAggregate.getName())
                .description(productAggregate.getDescription())
                .sku(productAggregate.getSku())
                .categoryId(productAggregate.getCategoryId())
                .status(productAggregate.getStatus())
                .price(productAggregate.getPrice())
                .isDel(productAggregate.getIsDel())
                .createTime(productAggregate.getCreateTime())
                .updateTime(productAggregate.getUpdateTime())
                .build();

        productDao.insert(product);
        return product.getId();
    }

    @Override
    public void deleteById(Long id) {
        if  (id == null) {
            throw new IllegalArgumentException("商品id不能为空");
        }
        productDao.deleteById(id);
    }

    @Override
    public ProductAggregate queryById(Long id) {
        if  (id == null) {
            throw new IllegalArgumentException("商品id不能为空");
        }
        Product product = productDao.queryById(id);
        if  (product == null) {
            return null;
        }
        return ProductAggregate.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .categoryId(product.getCategoryId())
                .status(product.getStatus())
                .price(product.getPrice())
                .isDel(product.getIsDel())
                .createTime(product.getCreateTime())
                .updateTime(product.getUpdateTime())
                .build();
    }

    @Override
    public void updateById(ProductAggregate updated) {
        if (updated == null) {
            throw new IllegalArgumentException("商品信息不能为空");
        }
        if (updated.getId() == null) {
            throw new IllegalArgumentException("商品id不能为空");
        }
        Product product = Product.builder()
                .id(updated.getId())
                .name(updated.getName())
                .description(updated.getDescription())
                .sku(updated.getSku())
                .categoryId(updated.getCategoryId())
                .status(updated.getStatus())
                .price(updated.getPrice())
                .isDel(updated.getIsDel())
                .createTime(updated.getCreateTime())
                .updateTime(updated.getUpdateTime())
                .build();
        productDao.update(product);
    }
}
