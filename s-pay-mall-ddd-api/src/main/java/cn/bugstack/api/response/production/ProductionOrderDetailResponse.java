package cn.bugstack.api.response.production;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductionOrderDetailResponse {

    private Long id;
    private String orderNo;
    private String requestNo;
    private Long productId;
    private Long productQuantity;
    private Long warehouseId;
    private Integer retryCount;
    private String failReason;
    private LocalDateTime nextRetryTime;
    private String failStage;
    private Integer needManualIntervention;
    private Integer status;
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<MaterialItem> materials;

    @Data
    public static class MaterialItem {

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
}
