package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.product.repository.IProductRepository;
import cn.bugstack.domain.producttype.model.aggregate.ProductTypeAggregate;
import cn.bugstack.domain.producttype.repository.IProductTypeRepository;
import cn.bugstack.infrastructure.adapter.repository.AbstractRepository;
import cn.bugstack.infrastructure.dao.IProductTypeDao;
import cn.bugstack.infrastructure.dao.po.product.ProductType;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ProductTypeRepository extends AbstractRepository implements IProductTypeRepository {

    @Resource
    private IProductRepository productRepository;

    @Resource
    private IProductTypeDao productTypeDao;

    @Override
    public Long save(ProductTypeAggregate productTypeAggregate) {
        if (productTypeAggregate == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品类型不能为空");
        }
        ProductType productType = ProductType.builder()
                .parentId(productTypeAggregate.getParentId() == null ? 0L : productTypeAggregate.getParentId())
                .name(productTypeAggregate.getName())
                .description(productTypeAggregate.getDescription())
                .typeCode(productTypeAggregate.getTypeCode())
                .sort(productTypeAggregate.getSort() == null ? 0 : productTypeAggregate.getSort())
                .status(productTypeAggregate.getStatus())
                .isDel(productTypeAggregate.getIsDel())
                .createTime(productTypeAggregate.getCreateTime())
                .updateTime(productTypeAggregate.getUpdateTime())
                .build();

        productTypeDao.insert(productType);
        return productType.getId();
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品分类id不能为空");
        }
        ProductTypeAggregate current = queryById(id);
        productTypeDao.deleteById(id);
        redisService.remove(cacheKeyById(id));
        if (current != null) {
            redisService.remove(cacheKeyByTypeCode(current.getTypeCode()));
        }
    }

    @Override
    public ProductTypeAggregate queryById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品分类id不能为空");
        }
        return getFromCacheOrDb(
                cacheKeyById(id),
                () -> {
                    ProductType productType = productTypeDao.queryById(id);
                    if (productType == null) {
                        return null;
                    }
                    return ProductTypeAggregate.builder()
                            .id(productType.getId())
                            .parentId(productType.getParentId())
                            .name(productType.getName())
                            .description(productType.getDescription())
                            .typeCode(productType.getTypeCode())
                            .sort(productType.getSort())
                            .status(productType.getStatus())
                            .isDel(productType.getIsDel())
                            .createTime(productType.getCreateTime())
                            .updateTime(productType.getUpdateTime())
                            .build();
                }
        );
    }

    @Override
    public ProductTypeAggregate queryByTypeCode(String typeCode) {
        if (typeCode == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品分类编码不能为空");
        }
        return getFromCacheOrDb(
                cacheKeyByTypeCode(typeCode),
                () -> {
                    ProductType productType = productTypeDao.queryByTypeCode(typeCode);
                    if (productType == null) {
                        return null;
                    }
                    return ProductTypeAggregate.builder()
                            .id(productType.getId())
                            .parentId(productType.getParentId())
                            .name(productType.getName())
                            .description(productType.getDescription())
                            .typeCode(productType.getTypeCode())
                            .sort(productType.getSort())
                            .status(productType.getStatus())
                            .isDel(productType.getIsDel())
                            .createTime(productType.getCreateTime())
                            .updateTime(productType.getUpdateTime())
                            .build();
                }
        );
    }

    @Override
    public void updateById(ProductTypeAggregate productTypeAggregate) {
        if (productTypeAggregate == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品类型不能为空");
        }
        ProductTypeAggregate current = queryById(productTypeAggregate.getId());
        ProductType productType = ProductType.builder()
                .id(productTypeAggregate.getId())
                .parentId(productTypeAggregate.getParentId() == null ? 0L : productTypeAggregate.getParentId())
                .name(productTypeAggregate.getName())
                .description(productTypeAggregate.getDescription())
                .typeCode(productTypeAggregate.getTypeCode())
                .sort(productTypeAggregate.getSort() == null ? 0 : productTypeAggregate.getSort())
                .status(productTypeAggregate.getStatus())
                .isDel(productTypeAggregate.getIsDel())
                .createTime(productTypeAggregate.getCreateTime())
                .updateTime(productTypeAggregate.getUpdateTime())
                .build();

        productTypeDao.update(productType);
        redisService.remove(cacheKeyById(productTypeAggregate.getId()));
        if (current != null) {
            redisService.remove(cacheKeyByTypeCode(current.getTypeCode()));
        }
        if (productTypeAggregate.getTypeCode() != null) {
            redisService.remove(cacheKeyByTypeCode(productTypeAggregate.getTypeCode()));
        }
    }

    @Override
    public long countProductByCategoryId(Long categoryId) {
        if (categoryId == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品分类id不能为空");
        }
        return productRepository.countByCategoryId(categoryId);
    }

    @Override
    public long countByParentId(Long parentId) {
        if (parentId == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "父分类id不能为空");
        }
        return productTypeDao.countByParentId(parentId);
    }

    @Override
    public List<ProductTypeAggregate> queryValidList() {
        return productTypeDao.queryValidList().stream()
                .map(productType -> ProductTypeAggregate.builder()
                        .id(productType.getId())
                        .parentId(productType.getParentId())
                        .name(productType.getName())
                        .description(productType.getDescription())
                        .typeCode(productType.getTypeCode())
                        .sort(productType.getSort())
                        .status(productType.getStatus())
                        .isDel(productType.getIsDel())
                        .createTime(productType.getCreateTime())
                        .updateTime(productType.getUpdateTime())
                        .build())
                .collect(Collectors.toList());
    }

    private String cacheKeyById(Long id) {
        return "s-pay-mall:product-type:id:" + id;
    }

    private String cacheKeyByTypeCode(String typeCode) {
        if (typeCode == null) {
            return null;
        }
        return "s-pay-mall:product-type:code:" + typeCode;
    }
}
