package cn.bugstack.infrastructure.dao.po.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCarousel {

    private Long id;
    private String title;
    private String coverImg;
    private String targetType;
    private String targetId;
    private String linkUrl;
    private Integer status;
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
