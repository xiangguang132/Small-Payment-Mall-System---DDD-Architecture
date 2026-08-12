package cn.bugstack.domain.groupbuy.service.trial.thread;

import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import cn.bugstack.domain.product.repository.IProductRepository;

import javax.annotation.Resource;
import java.util.concurrent.Callable;

public class QueryProductVOFromDBThreadTask implements Callable<ProductAggregate> {

    private final Long productId;

    @Resource
    private IProductRepository repository;

    public QueryProductVOFromDBThreadTask(Long productId, IProductRepository repository) {
        this.productId = productId;
        this.repository = repository;
    }

    @Override
    public ProductAggregate call() throws Exception {

        Long availableProductId = productId;
        if (availableProductId == null) {
            return null;
        }

        return repository.queryById(availableProductId);
    }
}
