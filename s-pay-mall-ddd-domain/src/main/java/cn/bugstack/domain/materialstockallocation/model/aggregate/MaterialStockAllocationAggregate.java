package cn.bugstack.domain.materialstockallocation.model.aggregate;

import cn.bugstack.domain.materialstockallocation.model.vo.MaterialStockAllocationItemVO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MaterialStockAllocationAggregate {

    private Long id;
    private String allocationNo;
    private Long materialId;
    private Long requestStockId;
    private BigDecimal requestQty;
    private BigDecimal lockedQty;
    private BigDecimal outboundQty;
    private BigDecimal releasedQty;
    private Integer status;
    private String reason;
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<MaterialStockAllocationItemVO> items;

}
