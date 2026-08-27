package cn.bugstack.domain.groupbuy.model.entity;

import cn.bugstack.domain.groupbuy.model.valobj.TrialRuleTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrialRuleResult {

    /** 规则类型 */
    private TrialRuleTypeEnum ruleType;

    /** 规则编码，例如 discountId、couponId 或积分配置编码 */
    private String ruleCode;

    /** 规则名称 */
    private String ruleName;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 当前价 */
    private BigDecimal currentPrice;

    /** 本次优惠金额 */
    private BigDecimal deductionPrice;

    /** 本次优惠后金额 */
    private BigDecimal payPrice;

    /** 是否可继续叠加后续规则 */
    private Boolean stackable;

    /** 是否命中规则 */
    private Boolean matched;

    /** 规则说明 */
    private String message;

}
