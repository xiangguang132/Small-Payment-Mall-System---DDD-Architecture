package cn.bugstack.api.request.product;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CarouselAddRequest {

    /** 轮播标题（展示用） */
    private String title;

    /** 轮播封面图URL */
    @NotBlank(message = "封面图不能为空")
    private String coverImg;

    /** 跳转类型：PRODUCT / GROUP_BUY / COUPON / LINK / NONE */
    @NotBlank(message = "跳转类型不能为空")
    private String targetType;

    /** 跳转目标ID */
    private String targetId;

    /** 外部链接地址 */
    private String linkUrl;

    /** 0-停用 1-启用 */
    private Integer status;
}
