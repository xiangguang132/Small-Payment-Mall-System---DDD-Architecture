package cn.bugstack.api.request.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartCheckoutRequest {

    /** 勾选的购物车项ID列表 */
    private List<Long> cartIds;

    /** 用户选择的优惠券ID列表（可空） */
    private List<String> couponIds;

}