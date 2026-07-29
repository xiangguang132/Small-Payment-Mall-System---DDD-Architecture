package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.producttype.model.aggregate.ProductTypeAggregate;
import cn.bugstack.domain.producttype.repository.IProductTypeRepository;
import cn.bugstack.infrastructure.dao.IProductTypeDao;
import cn.bugstack.infrastructure.dao.po.ProductType;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class ProductTypeRepository implements IProductTypeRepository {

    @Resource
    private IProductTypeDao productTypeDao;

    @Override
    public Long save(ProductTypeAggregate productTypeAggregate) {
        if (productTypeAggregate == null) {
            throw new IllegalArgumentException("商品类型不能为空");
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
            throw new IllegalArgumentException("商品分类id不能为空");
        }
        productTypeDao.deleteById(id);
    }

    @Override
    public ProductTypeAggregate queryById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("商品分类id不能为空");
        }
        ProductType productType = productTypeDao.queryById(id);
        if  (productType == null) {
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
}
