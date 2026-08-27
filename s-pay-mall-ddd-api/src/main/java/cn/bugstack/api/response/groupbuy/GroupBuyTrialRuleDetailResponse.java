package cn.bugstack.api.response.groupbuy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyTrialRuleDetailResponse {

    private String ruleType;

    private String ruleCode;

    private String ruleName;

    private BigDecimal originalPrice;

    private BigDecimal currentPrice;

    private BigDecimal deductionPrice;

    private BigDecimal payPrice;

    private Boolean stackable;

    private Boolean matched;

    private String message;

}