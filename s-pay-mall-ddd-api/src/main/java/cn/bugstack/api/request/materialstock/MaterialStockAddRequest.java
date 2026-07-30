package cn.bugstack.api.request.materialstock;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class MaterialStockAddRequest {

    @NotNull(message = "原料ID不能为空")
    private Long materialId;

    private String storageAddress;
}
