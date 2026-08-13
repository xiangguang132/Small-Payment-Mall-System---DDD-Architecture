package cn.bugstack.infrastructure.dao.po.supplier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Supplier {

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
