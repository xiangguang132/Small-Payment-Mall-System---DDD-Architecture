package cn.bugstack.api.response.product;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductDetailResponse {

    private Long id;
    private String name;
    private String description;
    private String sku;
    private Long categoryId;
    private Integer status;
    private BigDecimal price;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String categoryName;
    private String categoryDescription;
    private Long activityId;
}
