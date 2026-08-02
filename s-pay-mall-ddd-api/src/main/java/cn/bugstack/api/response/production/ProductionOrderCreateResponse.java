package cn.bugstack.api.response.production;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@Data
public class ProductionOrderCreateResponse {

    private Long id;
    private Long productionOrderId;
    private Long materialId;
    private Integer materialQuantity;
    private String allocationNo;
    private String status;
}