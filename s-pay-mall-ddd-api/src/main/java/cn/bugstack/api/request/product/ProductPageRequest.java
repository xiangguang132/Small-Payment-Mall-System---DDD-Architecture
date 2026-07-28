package cn.bugstack.api.request.product;

import lombok.Data;

@Data
public class ProductPageRequest {

    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String name;
    private String sku;
    private Long categoryId;
    private Integer status;
}
