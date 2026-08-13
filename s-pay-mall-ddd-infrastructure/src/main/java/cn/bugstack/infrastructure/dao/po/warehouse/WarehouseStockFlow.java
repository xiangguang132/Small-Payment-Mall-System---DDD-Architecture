package cn.bugstack.infrastructure.dao.po.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseStockFlow {

    private Long id;
    private Long warehouseId;
    private Long productId;
    private BigDecimal quantity;
    private String bizType;
    private String bizNo;
    private String reason;
    private Integer isDel;
    private LocalDateTime updateTime;
    private LocalDateTime createTime;

}
