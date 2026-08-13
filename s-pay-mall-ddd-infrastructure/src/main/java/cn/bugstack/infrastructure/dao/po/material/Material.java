package cn.bugstack.infrastructure.dao.po.material;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Material {

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
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
