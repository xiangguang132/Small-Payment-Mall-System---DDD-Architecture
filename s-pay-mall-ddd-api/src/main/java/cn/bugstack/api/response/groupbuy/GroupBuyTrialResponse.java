package cn.bugstack.api.response.groupbuy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyTrialResponse {

    // 活动信息
    private Long activityId;
    private String activityName;
    private Integer targetCount;
    private Integer validTime;

    // 商品信息
    private Long productId;
    private String productName;
    private BigDecimal originalPrice;

    // 试算结果
    /** 优惠减免金额，例如原价100、实付88时这里是12 */
    private BigDecimal deductionPrice;
    /** 最终实付金额，例如原价100、优惠12时这里是88 */
    private BigDecimal payPrice;
    /** 是否可见 */
    private Boolean visible;
    /** 是否可参与 */
    private Boolean enable;

    /** 每层规则的试算明细 */
    private List<GroupBuyTrialRuleDetailResponse> ruleDetails;

}