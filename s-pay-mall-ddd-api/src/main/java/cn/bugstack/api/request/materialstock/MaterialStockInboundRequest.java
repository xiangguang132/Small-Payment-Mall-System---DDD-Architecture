package cn.bugstack.api.request.materialstock;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class MaterialStockInboundRequest {

    @NotNull(message = "原料ID不能为空")
    private Long materialId;

    @NotBlank(message = "存储位置不能为空")
    private String storageAddress;

    @NotNull(message = "数量不能为空")
    @JsonAlias("quantity")
    private BigDecimal inboundQty;

    @NotBlank(message = "原因不能为空")
    private String reason;
}
