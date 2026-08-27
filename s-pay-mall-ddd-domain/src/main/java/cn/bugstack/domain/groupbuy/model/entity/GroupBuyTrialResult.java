package cn.bugstack.domain.groupbuy.model.entity;

import cn.bugstack.domain.groupbuy.model.valobj.TrialRuleTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyTrialResult {

    // 活动信息
    private Long activityId;
    private String activityName;
    private Integer groupType;
    private Integer takeLimitCount;
    private Integer targetCount;
    private Integer validTime;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String tagId;
    private String tagScope;

    // 商品信息
    private Long productId;
    private String productName;
    private BigDecimal originalPrice;

    // 折扣信息
    private String discountId;
    private String discountName;
    private String discountDesc;
    private Integer discountType;
    private String marketPlan;
    private String marketExpr;

    // 试算结果
    /** 优惠减免金额，例如原价100、实付88时这里是12 */
    private BigDecimal deductionPrice;
    /** 最终实付金额，例如原价100、优惠12时这里是88 */
    private BigDecimal payPrice;
    private Boolean visible;
    private Boolean enable;

    /** 规则链试算明细 */
    private List<TrialRuleResult> ruleDetails;

}