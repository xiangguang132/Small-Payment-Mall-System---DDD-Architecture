package cn.bugstack.api.request.materialtype;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class MaterialTypeAddRequest {

    private Long parentId;

    @NotBlank(message = "原料分类名称不能为空")
    private String name;

    private String description;

    @NotBlank(message = "原料分类编码不能为空")
    private String typeCode;

    @NotNull(message = "排序值不能为空")
    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort;

    @NotNull(message = "原料分类状态不能为空")
    @Min(value = 0, message = "原料分类状态值非法")
    @Max(value = 1, message = "原料分类状态值非法")
    private Integer status;
}
