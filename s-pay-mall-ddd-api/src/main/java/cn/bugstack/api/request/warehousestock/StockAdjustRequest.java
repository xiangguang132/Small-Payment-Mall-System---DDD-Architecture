package cn.bugstack.api.request.warehousestock;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class StockAdjustRequest {

    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "调整数量不能为空")
    private Integer quantity;

    private String reason;
}
