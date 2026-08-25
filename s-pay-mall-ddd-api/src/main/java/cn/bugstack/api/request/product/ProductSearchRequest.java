package cn.bugstack.api.request.product;

import lombok.Data;

@Data
public class ProductSearchRequest {

    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String keyword;
}
