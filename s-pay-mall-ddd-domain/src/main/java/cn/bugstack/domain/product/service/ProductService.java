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
    public ProductAggregate updateProduct(ProductAggregate product) {
        if (product == null) {
            throw new IllegalArgumentException("商品信息不能为空");
        }
        if (product.getId() == null) {
            throw new IllegalArgumentException("商品id不能为空");
        }

        ProductAggregate current = productRepository.queryById(product.getId());
        if (current == null) {
            throw new IllegalArgumentException("商品不存在");
        }

        ProductAggregate updated = ProductAggregate.builder()
                .id(current.getId())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .categoryId(product.getCategoryId())
                .status(product.getStatus())
                .price(product.getPrice())
                .isDel(current.getIsDel())
                .createTime(current.getCreateTime())
                .updateTime(LocalDateTime.now())
                .build();
        productRepository.update(updated);
        return updated;
    }

    @Override
    public ProductAggregate queryProductById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("商品id不能为空");
        }
        return productRepository.queryById(id);
    }
}
