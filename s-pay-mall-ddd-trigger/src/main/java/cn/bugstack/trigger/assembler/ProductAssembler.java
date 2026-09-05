package cn.bugstack.trigger.assembler;

import cn.bugstack.api.request.product.ProductAddRequest;
import cn.bugstack.api.response.page.PageResponse;
import cn.bugstack.api.response.product.ProductDetailResponse;
import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;

import java.util.List;
import java.util.stream.Collectors;

public class ProductAssembler {

    private ProductAssembler() {
    }

    public static ProductAggregate toAggregate(ProductAddRequest request) {
        if (request == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品信息不能为空");
        }
        return ProductAggregate.create(
                trim(request.getName()),
                trim(request.getDescription()),
                trim(request.getSku()),
                request.getCategoryId(),
                request.getStatus(),
                request.getPrice(),
                request.getCoveringImg(),
                request.getImgs()
        );
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    public static ProductDetailResponse toDetailResponse(ProductAggregate product) {
        if (product == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "暂无相关商品详情");
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
        response.setCoveringImg(product.getCoveringImg());
        response.setImgs(product.getImgs());
        response.setCreateTime(product.getCreateTime());
        response.setUpdateTime(product.getUpdateTime());
        return response;
    }

    public static PageResponse<ProductDetailResponse> toPageResponse(List<ProductAggregate> products, long total,
                                                                     Integer pageNo, Integer pageSize) {
        List<ProductDetailResponse> list = products == null ? java.util.Collections.emptyList()
                : products.stream()
                .map(ProductAssembler::toDetailResponse)
                .collect(Collectors.toList());
        return PageResponse.<ProductDetailResponse>builder()
                .total(total)
                .pageNo(pageNo)
                .pageSize(pageSize)
                .list(list)
                .build();
    }
}
