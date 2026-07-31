package cn.bugstack.domain.materialstockallocation.model.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MaterialStockAllocationItemVO {

    private Long stockId;
    private Long materialId;
    private String storageAddress;
    private BigDecimal allocateQty;
    private Integer sortNo;

}
