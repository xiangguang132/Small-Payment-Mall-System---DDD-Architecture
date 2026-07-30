package cn.bugstack.domain.warehouse.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WarehouseStatusVO {

    OFFLINE(0, "禁用"),
    ONLINE(1, "启用");

    private final Integer code;
    private final String desc;

    public static boolean isValid(Integer code) {
        if (code == null) {
            return false;
        }
        for (WarehouseStatusVO value : values()) {
            if (value.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
