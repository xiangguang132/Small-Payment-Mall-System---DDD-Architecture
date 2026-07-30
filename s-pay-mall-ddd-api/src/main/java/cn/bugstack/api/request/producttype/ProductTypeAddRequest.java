package cn.bugstack.api.request.producttype;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductTypeAddRequest {

    private Long parentId;

    @NotBlank(message = "商品分类名称不能为空")
    private String name;

    private String description;

    @NotBlank(message = "商品分类编码不能为空")
    private String typeCode;

    @NotNull(message = "排序值不能为空")
    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort;

    @NotNull(message = "商品分类状态不能为空")
    @Min(value = 0, message = "商品分类状态值非法")
    @Max(value = 1, message = "商品分类状态值非法")
    private Integer status;

}
