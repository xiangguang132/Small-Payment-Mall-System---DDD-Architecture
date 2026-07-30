package cn.bugstack.domain.producttype.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductTypeStatusVO {

    OFFLINE(0, "禁用"),
    ONLINE(1, "启用");

    private final Integer code;
    private final String desc;

    public static boolean isValid(Integer code) {
        if (code == null) {
            return false;
        }
        for (ProductTypeStatusVO value : values()) {
            if (value.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
