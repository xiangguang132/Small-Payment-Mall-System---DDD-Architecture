package cn.bugstack.domain.producttype.model.vo;

import cn.bugstack.domain.common.util.EnumCodeUtils;
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
        return EnumCodeUtils.isValid(code, values(), ProductTypeStatusVO::getCode);
    }
}
