package cn.bugstack.api.request.groupbuy;

import cn.bugstack.api.request.page.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取拼团活动
 * 依据market_plan字段过滤，使用 discount_id 联表 group_buy_activity 和 group_buy_discount 分页查询拼团的折扣活动
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GroupBuyActivityMarketPlanRequest extends PageRequest {

    /** 营销计划（ZJ:直减、MJ:满减、ZK:折扣、N:N元购） */
    private String marketPlan;

}
