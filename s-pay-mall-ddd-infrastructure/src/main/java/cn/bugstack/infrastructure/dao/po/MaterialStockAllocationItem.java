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
public class MaterialStockAllocationItem {

    private Long id;
    private Long allocationId;
    private Long stockId;
    private Long materialId;
    private String storageAddress;
    private BigDecimal allocateQty;
    private BigDecimal lockedQty;
    private BigDecimal outboundQty;
    private BigDecimal releasedQty;
    private Integer sortNo;
    private Integer status;
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
