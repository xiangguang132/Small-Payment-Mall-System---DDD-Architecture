package cn.bugstack.domain.production.model.aggregate;

import cn.bugstack.domain.production.model.vo.ProductionOrderMaterialVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionOrderAggregate {

    private Long id;
    private String orderNo;
    private Long productId;
    private Long productQuantity;
    private Long warehouseId;
    @Builder.Default
    private Integer status = 0;
    private Integer isDel = 0;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<ProductionOrderMaterialVO> materials;
}
