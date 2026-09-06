package cn.bugstack.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 轮播图跳转类型
 */
@Getter
@AllArgsConstructor
public enum CarouselTargetTypeEnum {

    PRODUCT("PRODUCT", "商品详情"),
    GROUP_BUY("GROUP_BUY", "拼团活动"),
    COUPON("COUPON", "优惠券"),
    LINK("LINK", "外部链接"),
    NONE("NONE", "不跳转"),
    ;

    private final String code;
    private final String info;

    public static boolean isValid(String code) {
        for (CarouselTargetTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
