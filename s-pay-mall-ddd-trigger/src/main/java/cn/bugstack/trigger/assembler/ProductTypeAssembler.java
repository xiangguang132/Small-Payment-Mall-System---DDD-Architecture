package cn.bugstack.trigger.assembler;

import cn.bugstack.api.request.producttype.ProductTypeAddRequest;
import cn.bugstack.api.request.producttype.ProductTypeUpdateRequest;
import cn.bugstack.api.response.producttype.ProductTypeDetailResponse;
import cn.bugstack.domain.producttype.model.aggregate.ProductTypeAggregate;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductTypeAssembler {

    private ProductTypeAssembler() {
    }

    public static ProductTypeAggregate toAggregate(ProductTypeAddRequest request) {
        if (request == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "品类信息不能为空");
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

    public static ProductTypeAggregate toUpdatedAggregate(ProductTypeAggregate current, ProductTypeUpdateRequest request) {
        if (current == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "商品分类不存在");
        }
        if (request == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "品类信息不能为空");
        }
        return ProductTypeAggregate.builder()
                .id(current.getId())
                .parentId(request.getParentId() != null ? request.getParentId() : current.getParentId())
                .name(request.getName() != null ? trim(request.getName()) : current.getName())
                .description(request.getDescription() != null ? trim(request.getDescription()) : current.getDescription())
                .typeCode(request.getTypeCode() != null ? trim(request.getTypeCode()) : current.getTypeCode())
                .sort(request.getSort() != null ? request.getSort() : current.getSort())
                .status(request.getStatus() != null ? request.getStatus() : current.getStatus())
                .isDel(current.getIsDel())
                .createTime(current.getCreateTime())
                .updateTime(current.getUpdateTime())
                .build();
    }

    public static ProductTypeDetailResponse toDetailResponse(ProductTypeAggregate productType) {
        if (productType == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "暂无相关商品分类详情");
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

    public static List<ProductTypeDetailResponse> toTreeResponseList(List<ProductTypeAggregate> productTypes) {
        if (productTypes == null || productTypes.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, ProductTypeDetailResponse> responseMap = new LinkedHashMap<>();
        for (ProductTypeAggregate productType : productTypes) {
            ProductTypeDetailResponse response = toDetailResponse(productType);
            response.setChildren(new ArrayList<>());
            responseMap.put(response.getId(), response);
        }
        List<ProductTypeDetailResponse> roots = new ArrayList<>();
        for (ProductTypeDetailResponse response : responseMap.values()) {
            Long parentId = response.getParentId();
            ProductTypeDetailResponse parent = parentId == null || parentId == 0L ? null : responseMap.get(parentId);
            if (parent != null && !parent.getId().equals(response.getId())) {
                parent.getChildren().add(response);
            } else {
                roots.add(response);
            }
        }
        return roots;
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
