package cn.bugstack.api.response.warehousestock;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StockDetailResponse {

    private Long id;
    private Long warehouseId;
    private Long productId;
    private BigDecimal availableQty;
    private BigDecimal lockedQty;
    private BigDecimal totalQty;
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
