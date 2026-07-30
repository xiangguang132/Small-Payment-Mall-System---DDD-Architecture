package cn.bugstack.api.request.materialstock;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class MaterialStockQuantityRequest {

    @NotNull(message = "数量不能为空")
    private Integer quantity;

    private String reason;
}
