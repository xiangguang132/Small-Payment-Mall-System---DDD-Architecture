package cn.bugstack.infrastructure.dao.po;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SupplierMaterial {

    private Long id;
    private Long supplierId;
    private Long materialId;
    private BigDecimal supplyPrice;
    private Integer leadTimeDays;
    private Integer status;
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
