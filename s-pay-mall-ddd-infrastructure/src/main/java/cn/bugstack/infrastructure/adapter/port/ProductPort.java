package cn.bugstack.infrastructure.adapter.port;

import cn.bugstack.domain.order.adapter.port.IProductPort;
import cn.bugstack.domain.order.model.entity.ProductEntity;
import cn.bugstack.infrastructure.dao.IProductDao;
import cn.bugstack.infrastructure.dao.po.product.Product;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ProductPort implements IProductPort {

    @Resource
    private IProductDao productDao;

    @Override
    public ProductEntity queryProductByProductId(String productId) {
        Product product = productDao.queryById(Long.parseLong(productId));
        if (product == null) {
            return null;
        }
        return ProductEntity.builder()
                .productId(String.valueOf(product.getId()))
                .productName(product.getName())
                .productDesc(product.getDescription())
                .price(product.getPrice())
                .build();
    }

}
