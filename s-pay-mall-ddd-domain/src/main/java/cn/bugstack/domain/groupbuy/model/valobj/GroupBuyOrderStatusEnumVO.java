package cn.bugstack.domain.groupbuy.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 参团订单状态
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum GroupBuyOrderStatusEnumVO {

    LOCKED(0, "已锁定"),
    PAID(1, "已支付"),
    REFUNDED(2, "已退款"),
    CANCELED(3, "已取消/退单");

    private Integer code;
    private String info;

    public static GroupBuyOrderStatusEnumVO valueOf(Integer code) {
        if (code == null) {
            return LOCKED;
        }
        switch (code) {
            case 0:
                return LOCKED;
            case 1:
                return PAID;
            case 2:
                return REFUNDED;
            case 3:
                return CANCELED;
            default:
                return LOCKED;
        }
    }

}
