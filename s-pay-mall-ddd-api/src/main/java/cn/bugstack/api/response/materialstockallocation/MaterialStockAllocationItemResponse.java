package cn.bugstack.api.response.materialstockallocation;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MaterialStockAllocationItemResponse {

    private Long stockId;
    private Long materialId;
    private String storageAddress;
    private BigDecimal allocateQty;
    private BigDecimal lockedQty;
    private BigDecimal outboundQty;
    private BigDecimal releasedQty;
    private Integer sortNo;
    private Integer status;
}
