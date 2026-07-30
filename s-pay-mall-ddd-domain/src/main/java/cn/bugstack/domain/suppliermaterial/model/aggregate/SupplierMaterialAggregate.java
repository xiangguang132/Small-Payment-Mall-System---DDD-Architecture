package cn.bugstack.domain.suppliermaterial.model.aggregate;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SupplierMaterialAggregate {

    private Long id;
    private Long supplierId;
    private Long materialId;
    private BigDecimal supplyPrice;
    private Integer leadTimeDays;
    private Integer status;
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public boolean enabled() {
        return status != null && status == 1 && (isDel == null || isDel == 0);
    }
}
