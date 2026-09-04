package cn.bugstack.api.request.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ToggleCheckRequest {

    /** 购物车项ID */
    private Long cartId;

    /** 是否选中；0否 1是 */
    private Integer checked;

}
