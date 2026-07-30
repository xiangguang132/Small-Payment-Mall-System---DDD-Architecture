package cn.bugstack.domain.producttype.service;

import cn.bugstack.domain.producttype.model.aggregate.ProductTypeAggregate;
import cn.bugstack.domain.producttype.model.vo.ProductTypeStatusVO;
import cn.bugstack.domain.producttype.repository.IProductTypeRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class ProductTypeService implements IProductTypeService {

    @Resource
    private IProductTypeRepository productTypeRepository;

    @Override
    public Long addNewProductType(ProductTypeAggregate productType) {
        if (productType == null) {
            throw new IllegalArgumentException("品类信息不能为空");
        }
        validateStatus(productType.getStatus());
        return productTypeRepository.save(productType);
    }

    @Override
    public void deleteProductTypeById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("商品分类id不能为空");
        }
        ProductTypeAggregate productTypeAggregate = productTypeRepository.queryById(id);
        if (productTypeAggregate == null) {
            throw new IllegalArgumentException("商品分类不存在");
        }

        long productCount = productTypeRepository.countProductByCategoryId(id);
        if (productCount > 0) {
            throw new IllegalArgumentException("商品分类已被商品使用，不能删除");
        }

        long childCount = productTypeRepository.countByParentId(id);
        if (childCount > 0) {
            throw new IllegalArgumentException("商品分类存在子分类，不能删除");
        }

        productTypeRepository.deleteById(id);
    }

    @Override
    public ProductTypeAggregate queryProductTypeById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("商品分类id不能为空");
        }
        return productTypeRepository.queryById(id);
    }

    @Override
    public ProductTypeAggregate onSale(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("商品分类id不能为空");
        }

        ProductTypeAggregate current = productTypeRepository.queryById(id);
        if (current == null) {
            throw new IllegalArgumentException("商品分类不存在");
        }

        Integer nextStatus = (current.getStatus() != null && current.getStatus() == 1) ? 0 : 1;
        ProductTypeAggregate updated = ProductTypeAggregate.builder()
                .id(current.getId())
                .parentId(current.getParentId())
                .name(current.getName())
                .description(current.getDescription())
                .typeCode(current.getTypeCode())
                .sort(current.getSort())
                .status(nextStatus)
                .isDel(current.getIsDel())
                .createTime(current.getCreateTime())
                .updateTime(LocalDateTime.now())
                .build();

        productTypeRepository.updateById(updated);
        return updated;
    }

    private void validateStatus(Integer status) {
        if (!ProductTypeStatusVO.isValid(status)) {
            throw new IllegalArgumentException("商品分类状态值非法");
        }
    }
}
