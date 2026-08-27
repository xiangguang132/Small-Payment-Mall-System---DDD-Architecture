package cn.bugstack.domain.groupbuy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPromotionRuleEntity {

    private Long id;

    /** 商品ID */
    private Long productId;

    /** 规则类型 */
    private Integer ruleType;

    /** 规则编码 */
    private String ruleCode;

    /** 执行顺序 */
    private Integer priority;

    /** 是否可叠加 */
    private Boolean stackable;

    /** 状态 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
