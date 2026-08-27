package cn.bugstack.infrastructure.dao.po.promotion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPromotionRule {

    private Long id;
    private Long productId;
    private Integer ruleType;
    private String ruleCode;
    private Integer priority;
    private Boolean stackable;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
