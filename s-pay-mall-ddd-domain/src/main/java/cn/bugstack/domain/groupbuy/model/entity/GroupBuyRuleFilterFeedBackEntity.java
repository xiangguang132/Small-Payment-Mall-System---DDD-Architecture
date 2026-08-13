package cn.bugstack.domain.groupbuy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 规则链-反馈实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyRuleFilterFeedBackEntity {

    private Integer userTakeOrderCount;

}
