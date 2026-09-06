package cn.bugstack.api.response.product;

import lombok.Data;

@Data
public class CarouselItemResponse {

    private Long id;
    private String title;
    private String coverImg;
    /** 跳转类型：PRODUCT / GROUP_BUY / COUPON / LINK / NONE */
    private String targetType;
    private String targetId;
    private String linkUrl;
    private Integer status;
}
