package cn.bugstack.domain.groupbuy.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 拼团订单展示态（组合 group_buy_order.status 与 group_buy_team.status，
 * 供"我的拼团"等前端列表按 tab 筛选与渲染）
 */
@Getter
@AllArgsConstructor
public enum GroupBuyOrderDisplayStatusVO {

    /** 锁单未支付 */
    PENDING_PAY(10, "待付款"),
    /** 已支付但团队未成团 */
    FORMING(20, "拼团中"),
    /** 已支付且已成团 */
    TEAM_COMPLETED(30, "已成团"),
    /** 已退款（含超时未付自动退款） */
    REFUNDED(40, "已退款");

    private final Integer code;
    private final String info;

    /**
     * 由订单状态与团队状态解析展示态
     * @param orderStatus group_buy_order.status：0锁单 1已支付 2已退款
     * @param teamStatus  group_buy_team.status：0拼团中 1已成团；可能为 null（团记录缺失）
     */
    public static GroupBuyOrderDisplayStatusVO resolve(Integer orderStatus, Integer teamStatus) {
        GroupBuyOrderStatusEnumVO order = GroupBuyOrderStatusEnumVO.valueOf(orderStatus);
        switch (order) {
            case LOCKED:
                return PENDING_PAY;
            case PAID:
                boolean teamCompleted = teamStatus != null && teamStatus == 1;
                return teamCompleted ? TEAM_COMPLETED : FORMING;
            case REFUNDED:
                return REFUNDED;
            default:
                return PENDING_PAY;
        }
    }

}
