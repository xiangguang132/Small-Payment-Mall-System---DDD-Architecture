package cn.bugstack.infrastructure.repository;


import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import cn.bugstack.domain.product.repository.IProductRepository;
import cn.bugstack.infrastructure.adapter.repository.AbstractRepository;
import cn.bugstack.infrastructure.dao.IProductDao;
import cn.bugstack.infrastructure.dao.po.product.Product;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ProductRepository extends AbstractRepository implements IProductRepository {

    @Resource
    private IProductDao productDao;

    @Override
    public Long save(ProductAggregate productAggregate) {
        if (productAggregate == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品信息不能为空");
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
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品id不能为空");
        }
        productDao.deleteById(id);
        redisService.remove(cacheKeyById(id));
    }

    @Override
    public ProductAggregate queryById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品id不能为空");
        }
        return getFromCacheOrDb(
                cacheKeyById(id),
                () -> {
                    Product product = productDao.queryById(id);
                    if (product == null) {
                        return null;
                    }
                    return ProductAggregate.builder()
                            .id(product.getId())
                            .name(product.getName())
                            .description(product.getDescription())
                            .sku(product.getSku())
                            .categoryId(product.getCategoryId())
                            .categoryName(product.getCategoryName())
                            .categoryDescription(product.getCategoryDescription())
                            .status(product.getStatus())
                            .price(product.getPrice())
                            .isDel(product.getIsDel())
                            .createTime(product.getCreateTime())
                            .updateTime(product.getUpdateTime())
                            .build();
                }
        );
    }

    @Override
    public void updateById(ProductAggregate updated) {
        if (updated == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品信息不能为空");
        }
        if (updated.getId() == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品id不能为空");
        }
        Product product = Product.builder()
                .id(updated.getId())
                .name(updated.getName())
                .description(updated.getDescription())
                .sku(updated.getSku())
                .categoryId(updated.getCategoryId())
                .categoryName(updated.getCategoryName())
                .categoryDescription(updated.getCategoryDescription())
                .status(updated.getStatus())
                .price(updated.getPrice())
                .isDel(updated.getIsDel())
                .createTime(updated.getCreateTime())
                .updateTime(updated.getUpdateTime())
                .build();
        productDao.update(product);
        redisService.remove(cacheKeyById(updated.getId()));
    }

    @Override
    public long countByCategoryId(Long categoryId) {
        if   (categoryId == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "分类id不能为空");
        }
        return productDao.countByCategoryId(categoryId);
    }

    @Override
    public long countPage(String name, String sku, Long categoryId, Integer status) {
        return productDao.countPage(name, sku, categoryId, status);
    }

    @Override
    public List<ProductAggregate> queryPage(String name, String sku, Long categoryId, Integer status,
                                            Integer offset, Integer limit) {
        return productDao.queryPage(name, sku, categoryId, status, offset, limit)
                .stream()
                .map(this::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public long countByKeyword(String keyword) {
        return productDao.countSearch(keyword);
    }

    @Override
    public List<ProductAggregate> queryByKeyword(String keyword, Integer offset, Integer limit) {
        return productDao.querySearch(keyword, offset, limit)
                .stream()
                .map(this::toAggregate)
                .collect(Collectors.toList());
    }

    private ProductAggregate toAggregate(Product product) {
        if (product == null) {
            return null;
        }
        return ProductAggregate.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .categoryId(product.getCategoryId())
                .categoryName(product.getCategoryName())
                .categoryDescription(product.getCategoryDescription())
                .status(product.getStatus())
                .price(product.getPrice())
                .isDel(product.getIsDel())
                .createTime(product.getCreateTime())
                .updateTime(product.getUpdateTime())
                .build();
    }

    private String cacheKeyById(Long id) {
        return "s-pay-mall:product:id:" + id;
    }
}
