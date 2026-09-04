package cn.bugstack.api.response.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartDetailResponse {

    /** 购物车项ID */
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 商品名称 */
    private String productName;

    /** 商品图片 */
    private String productImage;

    /** 商品价格 */
    private BigDecimal price;

    /** 数量 */
    private Integer quantity;

    /** 是否选中 */
    private Integer checked;

    /** 创建时间 */
    private LocalDateTime createTime;

}
