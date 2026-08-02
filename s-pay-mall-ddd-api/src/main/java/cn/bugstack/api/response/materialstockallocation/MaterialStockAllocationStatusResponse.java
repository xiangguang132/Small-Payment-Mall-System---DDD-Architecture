package cn.bugstack.api.response.materialstockallocation;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MaterialStockAllocationStatusResponse {

    private Long id;
    private String allocationNo;
    private Long materialId;
    private Long requestStockId;
    private BigDecimal requestQty;
    private BigDecimal lockedQty;
    private BigDecimal outboundQty;
    private BigDecimal releasedQty;
    private Integer status;
    private Integer retryCount;
    private String failReason;
    private String reason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
