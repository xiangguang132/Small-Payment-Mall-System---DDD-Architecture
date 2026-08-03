package cn.bugstack.domain.product.service;

import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import cn.bugstack.domain.product.model.vo.ProductStatusVO;
import cn.bugstack.domain.product.repository.IProductRepository;
import cn.bugstack.domain.producttype.model.aggregate.ProductTypeAggregate;
import cn.bugstack.domain.producttype.repository.IProductTypeRepository;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class ProductService implements IProductService {

    @Resource
    private IProductRepository productRepository;
    @Resource
    private IProductTypeRepository productTypeRepository;

    @Override
    public Long addNewProduct(ProductAggregate product) {
        if (product == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品信息不能为空");
        }
        validateStatus(product.getStatus());
        validateCategoryEnabled(product.getCategoryId());
        return productRepository.save(product);
    }

    @Override
    public void deleteProductById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品id不能为空");
        }
        ProductAggregate current = productRepository.queryById(id);
        if (current == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "商品不存在");
        }
        productRepository.deleteById(id);
    }

    @Override
    public ProductAggregate queryProductById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品id不能为空");
        }
        return productRepository.queryById(id);
    }

    @Override
    public void updateProductById(ProductAggregate updated) {
        if (updated == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品信息不能为空");
        }
        if (updated.getId() == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品id不能为空");
        }
        validateStatus(updated.getStatus());
        validateCategoryEnabled(updated.getCategoryId());
        productRepository.updateById(updated);
    }

    @Override
    public ProductAggregate onSale(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品id不能为空");
        }

        ProductAggregate current = productRepository.queryById(id);
        if (current == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "商品不存在");
        }

        Integer nextStatus = (current.getStatus() != null && current.getStatus() == 1) ? 0 : 1;
        ProductAggregate updated = ProductAggregate.builder()
                .id(current.getId())
                .name(current.getName())
                .description(current.getDescription())
                .sku(current.getSku())
                .categoryId(current.getCategoryId())
                .categoryName(current.getCategoryName())
                .categoryDescription(current.getCategoryDescription())
                .status(nextStatus)
                .price(current.getPrice())
                .isDel(current.getIsDel())
                .createTime(current.getCreateTime())
                .updateTime(LocalDateTime.now())
                .build();

        productRepository.updateById(updated);
        return updated;
    }

    private void validateCategoryEnabled(Long categoryId) {
        if (categoryId == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品分类id不能为空");
        }

        ProductTypeAggregate productType = productTypeRepository.queryById(categoryId);
        if (productType == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "商品分类不存在");
        }

        if (productType.getStatus() == null || productType.getStatus() != 1) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品分类未启用，不能使用");
        }
    }

    private void validateStatus(Integer status) {
        if (!ProductStatusVO.isValid(status)) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品状态值非法");
        }
    }
}
