package cn.bugstack.api.request.material;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class MaterialAddRequest {

    @NotBlank(message = "原料编码不能为空")
    private String materialCode;

    @NotBlank(message = "原料名称不能为空")
    private String name;

    @NotNull(message = "原料类型id不能为空")
    private Long typeId;

    @NotBlank(message = "计量单位不能为空")
    private String unit;

    private String description;

    @NotNull(message = "原料状态不能为空")
    @Min(value = 0, message = "原料状态值非法")
    @Max(value = 1, message = "原料状态值非法")
    private Integer status;
}
