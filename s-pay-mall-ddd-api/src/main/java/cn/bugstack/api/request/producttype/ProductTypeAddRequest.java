package cn.bugstack.api.request.producttype;

import lombok.Data;

@Data
public class ProductTypeAddRequest {

    private Long parentId;
    private String name;
    private String description;
    private String typeCode;
    private Integer sort;
    private Integer status;

}
