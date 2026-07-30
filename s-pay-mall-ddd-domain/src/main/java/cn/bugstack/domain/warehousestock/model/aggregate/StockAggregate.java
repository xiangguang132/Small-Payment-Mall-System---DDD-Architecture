package cn.bugstack.domain.warehousestock.model.aggregate;

import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class StockAggregate {

    private Long id;
    private Long warehouseId;
    private Long productId;
    private BigDecimal availableQty;
    private BigDecimal lockedQty;
    private BigDecimal totalQty;
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static StockAggregate create(Long warehouseId, Long productId) {
        return StockAggregate.builder()
                .warehouseId(warehouseId)
                .productId(productId)
                .availableQty(BigDecimal.ZERO)
                .lockedQty(BigDecimal.ZERO)
                .totalQty(BigDecimal.ZERO)
                .isDel(0)
                .build();
    }
}
