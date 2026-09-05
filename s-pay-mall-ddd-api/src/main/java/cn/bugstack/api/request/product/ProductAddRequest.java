package cn.bugstack.api.request.product;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductAddRequest {

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String description;

    @NotBlank(message = "商品SKU不能为空")
    private String sku;

    @NotNull(message = "商品分类id不能为空")
    private Long categoryId;

    @NotNull(message = "商品状态不能为空")
    @Min(value = 0, message = "商品状态值非法")
    @Max(value = 1, message = "商品状态值非法")
    private Integer status;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.00", inclusive = true, message = "商品价格不能小于0")
    private BigDecimal price;

    /** 封面图URL */
    private String coveringImg;

    /** 商品图片列表，多个URL */
    private String imgs;
}
