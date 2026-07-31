package cn.bugstack.domain.materialstockallocation.model.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MaterialStockAllocationItemVO {

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
