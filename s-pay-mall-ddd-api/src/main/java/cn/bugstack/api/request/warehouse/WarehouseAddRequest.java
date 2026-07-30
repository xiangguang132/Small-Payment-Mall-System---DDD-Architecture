package cn.bugstack.api.request.warehouse;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WarehouseAddRequest {

    @NotBlank(message = "仓库编码不能为空")
    private String warehouseCode;

    @NotBlank(message = "仓库名称不能为空")
    private String name;

    @NotNull(message = "仓库类型不能为空")
    @Min(value = 0, message = "仓库类型值非法")
    @Max(value = 1, message = "仓库类型值非法")
    private Integer type;

    private String address;

    private String contactName;

    private String contactPhone;

    @NotNull(message = "仓库状态不能为空")
    @Min(value = 0, message = "仓库状态值非法")
    @Max(value = 1, message = "仓库状态值非法")
    private Integer status;
}
