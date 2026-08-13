package cn.bugstack.infrastructure.dao.po.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductionOrderMaterial {

    private Long id;
    private Long productionOrderId;
    private Long materialId;
    private Integer materialQuantity;
    private String allocationNo;
    private Integer status;
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
