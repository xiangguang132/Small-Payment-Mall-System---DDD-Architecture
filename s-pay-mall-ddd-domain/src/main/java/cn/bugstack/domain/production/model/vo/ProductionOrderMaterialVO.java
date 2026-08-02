package cn.bugstack.domain.production.model.vo;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProductionOrderMaterialVO {
    private Long id;
    private Long productionOrderId;
    private Long materialId;
    private Integer materialQuantity;
    private String allocationNo;
    private Integer status;
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static ProductionOrderMaterialVO create(Long productionOrderId, Long materialId, Integer materialQuantity, String allocationNo, Integer status) {
        LocalDateTime now = LocalDateTime.now();
        return ProductionOrderMaterialVO.builder()
                .productionOrderId(productionOrderId)
                .materialId(materialId)
                .materialQuantity(materialQuantity)
                .allocationNo(allocationNo)
                .status(status)
                .isDel(0)
                .createTime(now)
                .updateTime(now)
                .build();
    }
}
