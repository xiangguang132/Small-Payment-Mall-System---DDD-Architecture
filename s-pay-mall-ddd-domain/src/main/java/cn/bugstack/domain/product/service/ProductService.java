package cn.bugstack.domain.product.service;

import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import cn.bugstack.domain.product.repository.IProductRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class ProductService implements IProductService {

    @Resource
    private IProductRepository productRepository;

    @Override
    public Long addNewProduct(ProductAggregate product) {
        if (product == null) {
            throw new IllegalArgumentException("商品信息不能为空");
        }
        return productRepository.save(product);
    }

    @Override
    public void deleteProductById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("商品id不能为空");
        }
        productRepository.deleteById(id);
    }

    @Override
    public ProductAggregate queryProductById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("商品id不能为空");
        }
        return productRepository.queryById(id);
    }

    @Override
    public void updateProductById(ProductAggregate updated) {
        if (updated == null) {
            throw new IllegalArgumentException("商品信息不能为空");
        }
        if (updated.getId() == null) {
            throw new IllegalArgumentException("商品id不能为空");
        }
        productRepository.updateById(updated);
    }

    @Override
    public ProductAggregate onSale(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("商品id不能为空");
        }

        ProductAggregate current = productRepository.queryById(id);
        if (current == null) {
            throw new IllegalArgumentException("商品不存在");
        }

        Integer nextStatus = (current.getStatus() != null && current.getStatus() == 1) ? 0 : 1;
        ProductAggregate updated = ProductAggregate.builder()
                .id(current.getId())
                .name(current.getName())
                .description(current.getDescription())
                .sku(current.getSku())
                .categoryId(current.getCategoryId())
                .status(nextStatus)
                .price(current.getPrice())
                .isDel(current.getIsDel())
                .createTime(current.getCreateTime())
                .updateTime(LocalDateTime.now())
                .build();

        productRepository.updateById(updated);
        return updated;
    }
}
