package cn.bugstack.api.request.product;

import lombok.Data;

@Data
public class CarouselUpdateRequest {

    private String title;
    private String coverImg;
    private String targetType;
    private String targetId;
    private String linkUrl;
    private Integer status;
}
