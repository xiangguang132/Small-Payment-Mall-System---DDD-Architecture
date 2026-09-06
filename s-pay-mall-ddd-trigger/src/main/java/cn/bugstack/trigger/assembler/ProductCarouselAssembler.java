package cn.bugstack.trigger.assembler;

import cn.bugstack.api.request.product.CarouselAddRequest;
import cn.bugstack.api.request.product.CarouselUpdateRequest;
import cn.bugstack.api.response.product.CarouselItemResponse;
import cn.bugstack.domain.product.model.entity.ProductCarouselEntity;

public class ProductCarouselAssembler {

    private ProductCarouselAssembler() {
    }

    public static ProductCarouselEntity toAddEntity(CarouselAddRequest request) {
        return ProductCarouselEntity.builder()
                .title(request.getTitle())
                .coverImg(request.getCoverImg())
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .linkUrl(request.getLinkUrl())
                .status(request.getStatus() == null ? 1 : request.getStatus())
                .build();
    }

    public static ProductCarouselEntity toUpdateEntity(Long id, CarouselUpdateRequest request) {
        return ProductCarouselEntity.builder()
                .id(id)
                .title(request.getTitle())
                .coverImg(request.getCoverImg())
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .linkUrl(request.getLinkUrl())
                .status(request.getStatus())
                .build();
    }

    public static CarouselItemResponse toResponse(ProductCarouselEntity entity) {
        if (entity == null) {
            return null;
        }
        CarouselItemResponse response = new CarouselItemResponse();
        response.setId(entity.getId());
        response.setTitle(entity.getTitle());
        response.setCoverImg(entity.getCoverImg());
        response.setTargetType(entity.getTargetType());
        response.setTargetId(entity.getTargetId());
        response.setLinkUrl(entity.getLinkUrl());
        response.setStatus(entity.getStatus());
        return response;
    }
}
