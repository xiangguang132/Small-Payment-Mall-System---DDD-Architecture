package cn.bugstack.api.request.production;

import cn.bugstack.api.request.page.PageRequest;
import lombok.Data;

@Data
public class ProductionOrderListRequest extends PageRequest {

    private Integer status;
    private Long productId;
    private Long warehouseId;
}
