package cn.bugstack.api.response.product;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品列表精简响应 — 仅包含列表页所需字段
 */
@Data
public class ProductListItemResponse {

    private Long id;
    private String name;
    private BigDecimal price;

    /** 封面图URL */
    private String coveringImg;

    /** 拼团活动ID（有拼团活动时填充） */
    private Long activityId;

    /** 拼团成团人数（有拼团活动时填充） */
    private Integer targetCount;
}
