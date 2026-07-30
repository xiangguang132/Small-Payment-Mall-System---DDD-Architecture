package cn.bugstack.domain.material.model.aggregate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialAggregate {

    private Long id;
    private String materialCode;
    private String name;
    private Long typeId;
    private String typeName;
    private String typeDescription;
    private String typeCode;
    private String unit;
    private String description;
    private Integer status;
    @Builder.Default
    private Integer isDel = 0;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static MaterialAggregate create(String materialCode, String name, Long typeId,
                                           String unit, String description, Integer status) {
        LocalDateTime now = LocalDateTime.now();
        return MaterialAggregate.builder()
                .materialCode(materialCode)
                .name(name)
                .typeId(typeId)
                .unit(unit)
                .description(description)
                .status(status)
                .isDel(0)
                .createTime(now)
                .updateTime(now)
                .build();
    }
}
