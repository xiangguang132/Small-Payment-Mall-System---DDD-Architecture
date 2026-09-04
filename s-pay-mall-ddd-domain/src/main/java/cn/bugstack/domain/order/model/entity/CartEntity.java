package cn.bugstack.domain.order.model.entity;

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
public class CartEntity {

    /** 购物车项ID */
    private Long id;

    /** 用户ID */
    private String userId;

    /** 商品ID */
    private Long productId;

    /** 数量 */
    private Integer quantity;

    /** 是否选中；0否 1是 */
    private Integer checked;

    /** 状态；0删除 1正常 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    // ─── 联表 product 冗余字段 ───

    /** 商品名称 */
    private String productName;

    /** 商品价格 */
    private BigDecimal price;

    /** 商品图片 */
    private String productImage;

}
