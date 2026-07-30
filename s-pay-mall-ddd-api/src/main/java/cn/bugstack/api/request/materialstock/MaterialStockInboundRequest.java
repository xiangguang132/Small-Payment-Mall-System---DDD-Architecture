package cn.bugstack.api.request.materialstock;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class MaterialStockInboundRequest {

    @NotNull(message = "供应商原料关系ID不能为空")
    private Long supplierMaterialId;

    private String storageAddress;

    @NotNull(message = "入库数量不能为空")
    private Integer quantity;

    private String reason;
}
