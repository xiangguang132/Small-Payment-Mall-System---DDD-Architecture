package cn.bugstack.api.request.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddToCartRequest {

    /** 商品ID */
    private Long productId;

    /** 数量，默认1 */
    @Builder.Default
    private Integer quantity = 1;

}
