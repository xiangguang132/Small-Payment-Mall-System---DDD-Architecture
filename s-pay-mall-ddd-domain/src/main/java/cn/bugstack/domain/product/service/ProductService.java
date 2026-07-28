package cn.bugstack.domain.product.service;

import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import cn.bugstack.domain.product.repository.IProductRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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
    public ProductAggregate updateProduct(ProductAggregate product) {
        if (product == null) {
            throw new IllegalArgumentException("商品信息不能为空");
        }
        if (product.getId() == null) {
            throw new IllegalArgumentException("商品id不能为空");
        }
        int affectedRows = productRepository.update(product);
        if (affectedRows <= 0) {
            throw new IllegalStateException("商品不存在或已删除");
        }
        return productRepository.queryById(product.getId());
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
}
