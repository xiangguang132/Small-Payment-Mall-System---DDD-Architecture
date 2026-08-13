package cn.bugstack.infrastructure.dao.po.material;

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
public class MaterialStock {

    private Long id;
    private Long materialId;
    private String storageAddress;
    private BigDecimal availableQty;
    private BigDecimal lockedQty;
    private BigDecimal totalQty;
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
