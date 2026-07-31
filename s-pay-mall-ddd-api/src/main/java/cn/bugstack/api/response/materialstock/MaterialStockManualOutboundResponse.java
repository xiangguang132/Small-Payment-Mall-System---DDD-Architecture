package cn.bugstack.api.response.materialstock;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MaterialStockManualOutboundResponse {

    private String storageAddress;
    private BigDecimal availableQty;
    private BigDecimal lockedQty;
    private BigDecimal totalQty;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
