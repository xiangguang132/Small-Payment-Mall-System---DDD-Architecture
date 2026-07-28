package cn.bugstack.infrastructure.dao.po;

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
public class Product {
    /** 商品ID (对应 bigint) */
    private Long id;

    /** 商品名称 (对应 varchar 128) */
    private String name;

    /** 商品描述 (对应 varchar 512) */
    private String description;

    /** 商品SKU编码 (对应 varchar 64) */
    private String sku;

    /** 分类ID (对应 bigint) */
    private Long categoryId;

    /** 状态 0下架 1上架 (对应 tinyint) */
    private Integer status;

    /** 商品价格 (对应 decimal 18,2) */
    private BigDecimal price;

    /** 是否删除 0否 1是 (对应 tinyint) */
    private Integer isDel;

    /** 创建时间 (对应 datetime) */
    private LocalDateTime createTime;

    /** 更新时间 (对应 datetime) */
    private LocalDateTime updateTime;

}
