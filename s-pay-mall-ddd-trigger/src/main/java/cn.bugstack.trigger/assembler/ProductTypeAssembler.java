package cn.bugstack.trigger.assembler;

import cn.bugstack.api.request.producttype.ProductTypeAddRequest;
import cn.bugstack.api.response.producttype.ProductTypeDetailResponse;
import cn.bugstack.domain.producttype.model.aggregate.ProductTypeAggregate;

public class ProductTypeAssembler {

    private ProductTypeAssembler() {
    }

    public static ProductTypeAggregate toAggregate(ProductTypeAddRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("品类信息不能为空");
        }
        return ProductTypeAggregate.create(
                request.getParentId(),
                trim(request.getName()),
                trim(request.getDescription()),
                trim(request.getTypeCode()),
                request.getSort(),
                request.getStatus()
        );
    }

    public static ProductTypeDetailResponse toDetailResponse(ProductTypeAggregate productType) {
        if (productType == null) {
            throw new IllegalArgumentException("暂无相关商品分类详情");
        }
        ProductTypeDetailResponse response = new ProductTypeDetailResponse();
        response.setId(productType.getId());
        response.setParentId(productType.getParentId());
        response.setName(productType.getName());
        response.setDescription(productType.getDescription());
        response.setTypeCode(productType.getTypeCode());
        response.setSort(productType.getSort());
        response.setStatus(productType.getStatus());
        response.setIsDel(productType.getIsDel());
        response.setCreateTime(productType.getCreateTime());
        response.setUpdateTime(productType.getUpdateTime());
        return response;
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
