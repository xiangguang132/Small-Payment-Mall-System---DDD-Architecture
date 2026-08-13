package cn.bugstack.infrastructure.dao.po.product;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductionOrder {

    private Long id;
    private String orderNo;
    private Long productId;
    private String requestNo;
    private Long productQuantity;
    private Long warehouseId;
    private Integer retryCount;
    private String failReason;
    private LocalDateTime nextRetryTime;
    private Integer status;
    private String failStage;
    private Integer needManualIntervention;
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
