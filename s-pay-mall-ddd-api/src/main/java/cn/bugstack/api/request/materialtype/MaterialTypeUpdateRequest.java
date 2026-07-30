package cn.bugstack.api.request.materialtype;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
public class MaterialTypeUpdateRequest {

    private Long parentId;

    private String name;

    private String description;

    private String typeCode;

    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort;

    @Min(value = 0, message = "原料分类状态值非法")
    @Max(value = 1, message = "原料分类状态值非法")
    private Integer status;
}
