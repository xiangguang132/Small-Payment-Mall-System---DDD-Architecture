package cn.bugstack.domain.materialtype.model.aggregate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialTypeAggregate {

    private Long id;
    private Long parentId;
    private String name;
    private String description;
    private String typeCode;
    private Integer sort;
    private Integer status;
    @Builder.Default
    private Integer isDel = 0;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static MaterialTypeAggregate create(Long parentId, String name, String description,
                                               String typeCode, Integer sort, Integer status) {
        LocalDateTime now = LocalDateTime.now();
        return MaterialTypeAggregate.builder()
                .parentId(parentId)
                .name(name)
                .description(description)
                .typeCode(typeCode)
                .sort(sort)
                .status(status)
                .isDel(0)
                .createTime(now)
                .updateTime(now)
                .build();
    }

    public void changeStatus(Integer status) {
        this.status = status;
    }
}
