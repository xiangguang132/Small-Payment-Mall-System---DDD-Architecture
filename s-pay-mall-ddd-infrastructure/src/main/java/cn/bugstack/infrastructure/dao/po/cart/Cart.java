package cn.bugstack.infrastructure.dao.po.cart;

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
public class Cart {

    private Long id;

    private String userId;

    private Long productId;

    private Integer quantity;

    private Integer checked;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // ─── 联表 product 字段 ───

    private String productName;

    private BigDecimal productPrice;

    private String productImage;

}
