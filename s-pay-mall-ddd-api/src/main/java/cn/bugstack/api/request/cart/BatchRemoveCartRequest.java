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
public class BatchRemoveCartRequest {

    /** 购物车项ID列表 */
    private List<Long> cartIds;

}
