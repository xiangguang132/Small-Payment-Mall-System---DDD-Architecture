package cn.bugstack.api.request.production;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@Data
public class ProductionOrderCreateRequest {

    @NotNull(message = "生产商品ID不能为空")
    private Long productId;

    @NotNull(message = "生产数量不能为空")
    @Positive(message = "生产数量必须大于0")
    private Integer productQuantity;

    @NotNull(message = "入库仓库ID不能为空")
    private Long warehouseId;

    @Valid
    @NotEmpty(message = "生产原料不能为空")
    private List<MaterialItem> materials;

    @Data
    public static class MaterialItem {

        @NotNull(message = "原料ID不能为空")
        private Long materialId;

        @NotNull(message = "原料数量不能为空")
        @Positive(message = "原料数量必须大于0")
        private Integer quantity;
    }
}