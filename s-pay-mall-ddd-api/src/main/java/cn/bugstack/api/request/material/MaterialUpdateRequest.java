package cn.bugstack.api.request.material;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
public class MaterialUpdateRequest {

    private String materialCode;

    private String name;

    private Long typeId;

    private String unit;

    private String description;

    @Min(value = 0, message = "原料状态值非法")
    @Max(value = 1, message = "原料状态值非法")
    private Integer status;
}
