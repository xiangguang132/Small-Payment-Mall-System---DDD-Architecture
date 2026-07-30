package cn.bugstack.domain.supplier.model.aggregate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierAggregate {

    private Long id;
    private String supplierCode;
    private String name;
    private String contactName;
    private String contactPhone;
    private String address;
    private Integer status;
    @Builder.Default
    private Integer isDel = 0;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static SupplierAggregate create(String supplierCode, String name,
                                           String contactName, String contactPhone,
                                           String address, Integer status) {
        LocalDateTime now = LocalDateTime.now();
        return SupplierAggregate.builder()
                .supplierCode(supplierCode)
                .name(name)
                .contactName(contactName)
                .contactPhone(contactPhone)
                .address(address)
                .status(status)
                .isDel(0)
                .createTime(now)
                .updateTime(now)
                .build();
    }
}
