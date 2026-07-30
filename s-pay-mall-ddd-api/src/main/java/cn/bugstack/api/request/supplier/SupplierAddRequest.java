package cn.bugstack.api.request.supplier;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SupplierAddRequest {

    @NotBlank(message = "供应商编码不能为空")
    private String supplierCode;

    @NotBlank(message = "供应商名称不能为空")
    private String name;

    private String contactName;

    private String contactPhone;

    private String address;

    @NotNull(message = "供应商状态不能为空")
    @Min(value = 0, message = "供应商状态值非法")
    @Max(value = 1, message = "供应商状态值非法")
    private Integer status;
}
