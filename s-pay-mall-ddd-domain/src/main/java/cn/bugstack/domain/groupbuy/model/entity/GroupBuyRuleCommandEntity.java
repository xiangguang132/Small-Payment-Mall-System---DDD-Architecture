package cn.bugstack.domain.groupbuy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 规则链-定义规则实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyRuleCommandEntity {

    private String userId;
    private Long activityId;
    private String outTradeNo;

}
