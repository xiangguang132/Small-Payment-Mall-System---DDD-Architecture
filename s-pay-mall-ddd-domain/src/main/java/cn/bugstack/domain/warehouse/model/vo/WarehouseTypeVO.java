package cn.bugstack.domain.warehouse.model.vo;

import cn.bugstack.domain.common.util.EnumCodeUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WarehouseTypeVO {

    SELF(0, "自营仓"),
    THIRD_PARTY(1, "第三方仓");

    private final Integer code;
    private final String desc;

    public static boolean isValid(Integer code) {
        return EnumCodeUtils.isValid(code, values(), WarehouseTypeVO::getCode);
    }
}
