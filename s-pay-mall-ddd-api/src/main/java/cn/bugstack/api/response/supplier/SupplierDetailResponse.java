package cn.bugstack.api.response.supplier;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplierDetailResponse {

    private Long id;
    private String supplierCode;
    private String name;
    private String contactName;
    private String contactPhone;
    private String address;
    private Integer status;
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
