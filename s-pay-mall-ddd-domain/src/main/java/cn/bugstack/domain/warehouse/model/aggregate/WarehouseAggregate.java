package cn.bugstack.domain.warehouse.model.aggregate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseAggregate {

    private Long id;
    private String warehouseCode;
    private String name;
    private Integer type;
    private String address;
    private String contactName;
    private String contactPhone;
    private Integer status;
    @Builder.Default
    private Integer isDel = 0;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static WarehouseAggregate create(String warehouseCode, String name, Integer type,
                                            String address, String contactName, String contactPhone, Integer status) {
        LocalDateTime now = LocalDateTime.now();
        return WarehouseAggregate.builder()
                .warehouseCode(warehouseCode)
                .name(name)
                .type(type)
                .address(address)
                .contactName(contactName)
                .contactPhone(contactPhone)
                .status(status)
                .isDel(0)
                .createTime(now)
                .updateTime(now)
                .build();
    }
}
