package cn.bugstack.api.response.producttype;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductTypeDetailResponse {

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
