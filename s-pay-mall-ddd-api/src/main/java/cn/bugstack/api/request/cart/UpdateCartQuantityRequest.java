package cn.bugstack.api.request.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCartQuantityRequest {

    /** 购物车项ID */
    private Long cartId;

    /** 数量 */
    private Integer quantity;

}
