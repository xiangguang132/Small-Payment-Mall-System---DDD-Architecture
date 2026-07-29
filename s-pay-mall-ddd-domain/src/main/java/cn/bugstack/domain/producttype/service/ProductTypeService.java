package cn.bugstack.domain.producttype.service;

import cn.bugstack.domain.producttype.model.aggregate.ProductTypeAggregate;
import cn.bugstack.domain.producttype.repository.IProductTypeRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ProductTypeService implements IProductTypeService {

    @Resource
    private IProductTypeRepository productTypeRepository;

    @Override
    public Long addNewProductType(ProductTypeAggregate productType) {
        if (productType == null) {
            throw new IllegalArgumentException("品类信息不能为空");
        }
        return productTypeRepository.save(productType);
    }

    @Override
    public void deleteProductTypeById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("商品分类id不能为空");
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
}
