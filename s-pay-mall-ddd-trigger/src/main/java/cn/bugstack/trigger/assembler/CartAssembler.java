package cn.bugstack.trigger.assembler;

import cn.bugstack.api.response.cart.CartDetailResponse;
import cn.bugstack.domain.order.model.entity.CartEntity;

import java.util.List;
import java.util.stream.Collectors;

public class CartAssembler {

    private CartAssembler() {}

    public static List<CartDetailResponse> toDetailResponse(List<CartEntity> entities) {
        if (entities == null) return null;
        return entities.stream().map(CartAssembler::toDetailResponse).collect(Collectors.toList());
    }

    public static CartDetailResponse toDetailResponse(CartEntity entity) {
        if (entity == null) return null;
        return CartDetailResponse.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .productName(entity.getProductName())
                .productImage(entity.getProductImage())
                .price(entity.getPrice())
                .quantity(entity.getQuantity())
                .checked(entity.getChecked())
                .createTime(entity.getCreateTime())
                .build();
    }

}
