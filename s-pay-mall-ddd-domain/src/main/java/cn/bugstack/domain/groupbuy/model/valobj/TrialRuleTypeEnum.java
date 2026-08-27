package cn.bugstack.domain.groupbuy.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 试算规则类型
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum TrialRuleTypeEnum {

    GROUP_BUY(0, "拼团优惠"),
    COUPON(1, "优惠券"),
    POINTS(2, "积分抵扣"),
    ;

    private Integer code;
    private String info;

    public static TrialRuleTypeEnum get(Integer code) {
        switch (code) {
            case 0:
                return GROUP_BUY;
            case 1:
                return COUPON;
            case 2:
                return POINTS;
            default:
                throw new RuntimeException("err code!");
        }
    }
}
