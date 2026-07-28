package cn.bugstack.api.request.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUpdateRequest {

    private Long id;
    private String name;
    private String description;
    private String sku;
    private Long categoryId;
    private Integer status;
    private BigDecimal price;
}
