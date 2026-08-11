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
public class GroupBuyDiscountEntity {

    private Long id;

    private String discountId;

    private String discountName;

    private String discountDesc;

    private Integer discountType;

    private String marketPlan;

    private String marketExpr;

    private String tagId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
