package cn.bugstack.trigger.assembler;

import cn.bugstack.api.request.product.ProductAddRequest;
import cn.bugstack.api.response.product.ProductDetailResponse;
import cn.bugstack.domain.product.model.aggregate.ProductAggregate;

public class ProductAssembler {

    private ProductAssembler() {
    }

    public static ProductAggregate toAggregate(ProductAddRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("商品信息不能为空");
        }
        return ProductAggregate.create(
                trim(request.getName()),
                trim(request.getDescription()),
                trim(request.getSku()),
                request.getCategoryId(),
                request.getStatus(),
                request.getPrice()
        );
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    public static ProductDetailResponse toDetailResponse(ProductAggregate product) {
        if (product == null) {
            throw new IllegalArgumentException("暂无相关商品详情");
        }
        ProductDetailResponse response = new ProductDetailResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setSku(product.getSku());
        response.setCategoryId(product.getCategoryId());
        response.setCategoryName(product.getCategoryName());
        response.setCategoryDescription(product.getCategoryDescription());
        response.setStatus(product.getStatus());
        response.setPrice(product.getPrice());
        response.setCreateTime(product.getCreateTime());
        response.setUpdateTime(product.getUpdateTime());
        return response;
    }
}
