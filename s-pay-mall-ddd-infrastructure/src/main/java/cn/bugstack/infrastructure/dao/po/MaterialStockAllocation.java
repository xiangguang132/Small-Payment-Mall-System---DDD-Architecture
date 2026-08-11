package cn.bugstack.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialStockAllocation {

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
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
