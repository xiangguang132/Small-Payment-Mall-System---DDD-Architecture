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

    /** 封面图URL */
    private String coveringImg;

    /** 商品图片列表，多个URL */
    private String imgs;

    private String categoryName;
    private String categoryDescription;
    private Long activityId;

    // 拼团试算结果（有拼团活动时填充）
    private BigDecimal trialPayPrice;
    private Integer targetCount;
}
