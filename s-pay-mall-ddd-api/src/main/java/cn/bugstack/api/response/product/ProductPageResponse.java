package cn.bugstack.api.response.product;

import lombok.Data;

import java.util.List;

@Data
public class ProductPageResponse {

    private Long total;
    private List<ProductDetailResponse> list;
}
