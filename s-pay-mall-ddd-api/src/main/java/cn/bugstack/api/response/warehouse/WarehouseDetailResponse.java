package cn.bugstack.api.response.warehouse;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WarehouseDetailResponse {

    private Long id;
    private String warehouseCode;
    private String name;
    private Integer type;
    private String address;
    private String contactName;
    private String contactPhone;
    private Integer status;
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
