package cn.bugstack.domain.product.model.vo;

import cn.bugstack.domain.common.util.EnumCodeUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductStatusVO {

    OFFLINE(0, "下架"),
    ONLINE(1, "上架");

    private final Integer code;
    private final String desc;

    public static ProductStatusVO valueOf(Integer code) {
        if (code == null) {
            return OFFLINE;
        }
        for (ProductStatusVO value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return OFFLINE;
    }

    public static boolean isValid(Integer code) {
        return EnumCodeUtils.isValid(code, values(), ProductStatusVO::getCode);
    }
}
