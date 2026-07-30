package cn.bugstack.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialType {

    private Long id;

    private Long parentId;

    private String name;

    private String description;

    private String typeCode;

    private Integer sort;

    private Integer status;

    private Integer isDel;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
