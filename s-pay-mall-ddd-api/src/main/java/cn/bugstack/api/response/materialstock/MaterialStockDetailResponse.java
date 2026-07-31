package cn.bugstack.api.response.materialstock;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MaterialStockDetailResponse {

    private Long id;
    private Long materialId;
    private String storageAddress;
    private BigDecimal availableQty;
    private BigDecimal lockedQty;
    private BigDecimal totalQty;
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
