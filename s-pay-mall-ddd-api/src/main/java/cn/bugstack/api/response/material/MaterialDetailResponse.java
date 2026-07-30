package cn.bugstack.api.response.material;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MaterialDetailResponse {

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
