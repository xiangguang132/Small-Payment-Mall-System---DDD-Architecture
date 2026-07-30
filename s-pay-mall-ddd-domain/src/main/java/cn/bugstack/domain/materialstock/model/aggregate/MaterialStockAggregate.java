package cn.bugstack.domain.materialstock.model.aggregate;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MaterialStockAggregate {

    private Long id;
    private Long materialId;
    private String storageAddress;
    private BigDecimal availableQty;
    private BigDecimal lockedQty;
    private BigDecimal totalQty;
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static MaterialStockAggregate create(Long materialId, String storageAddress) {
        return MaterialStockAggregate.builder()
                .materialId(materialId)
                .storageAddress(storageAddress)
                .availableQty(BigDecimal.ZERO)
                .lockedQty(BigDecimal.ZERO)
                .totalQty(BigDecimal.ZERO)
                .isDel(0)
                .build();
    }
}
