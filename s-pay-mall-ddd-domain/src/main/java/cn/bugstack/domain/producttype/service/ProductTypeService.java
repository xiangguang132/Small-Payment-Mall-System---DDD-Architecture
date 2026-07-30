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
    public void updateProductTypeById(ProductTypeAggregate updated) {
        if (updated == null) {
            throw new IllegalArgumentException("商品分类不能为空");
        }
        if (updated.getId() == null) {
            throw new IllegalArgumentException("商品分类id不能为空");
        }

        ProductTypeAggregate current = productTypeRepository.queryById(updated.getId());
        if (current == null) {
            throw new IllegalArgumentException("商品分类不存在");
        }

        if (updated.getParentId() != null && !updated.getParentId().equals(current.getParentId())) {
            validateParentId(updated.getParentId(), updated.getId());
        }
        validateName(updated.getName());
        validateTypeCode(updated.getTypeCode());
        validateSort(updated.getSort());
        validateStatus(updated.getStatus());
        validateUniqueTypeCode(updated.getTypeCode(), updated.getId());

        ProductTypeAggregate productType = ProductTypeAggregate.builder()
                .id(current.getId())
                .parentId(updated.getParentId())
                .name(updated.getName())
                .description(updated.getDescription())
                .typeCode(updated.getTypeCode())
                .sort(updated.getSort())
                .status(updated.getStatus())
                .isDel(current.getIsDel())
                .createTime(current.getCreateTime())
                .updateTime(LocalDateTime.now())
                .build();
        productTypeRepository.updateById(productType);
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

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("商品分类名称不能为空");
        }
    }

    private void validateTypeCode(String typeCode) {
        if (typeCode == null || typeCode.trim().isEmpty()) {
            throw new IllegalArgumentException("商品分类编码不能为空");
        }
    }

    private void validateSort(Integer sort) {
        if (sort == null) {
            throw new IllegalArgumentException("排序值不能为空");
        }
        if (sort < 0) {
            throw new IllegalArgumentException("排序值不能小于0");
        }
    }

    private void validateParentId(Long parentId, Long currentId) {
        if (parentId == null || parentId == 0L) {
            return;
        }
        if (parentId < 0) {
            throw new IllegalArgumentException("父分类id不能小于0");
        }
        if (currentId != null && parentId.equals(currentId)) {
            throw new IllegalArgumentException("父分类不能是当前分类本身");
        }

        ProductTypeAggregate parent = productTypeRepository.queryById(parentId);
        if (parent == null) {
            throw new IllegalArgumentException("父分类不存在");
        }
        if (parent.getStatus() == null || parent.getStatus() != 1) {
            throw new IllegalArgumentException("父分类未启用，不能使用");
        }
    }

    private void validateUniqueTypeCode(String typeCode, Long currentId) {
        ProductTypeAggregate exist = productTypeRepository.queryByTypeCode(typeCode);
        if (exist != null && !exist.getId().equals(currentId)) {
            throw new IllegalArgumentException("商品分类编码已存在");
        }
    }
}
