package cn.bugstack.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@AllArgsConstructor
@Getter
public enum RoleEnum {

    CUSTOMER(0, "顾客"),
    ADMIN(1, "管理员"),
    INVENTORY_MANAGER(2, "原料库存管理人员");

    private final int code;
    private final String info;

    /**
     * 根据 code 获取枚举，未知值默认返回顾客
     */
    public static RoleEnum of(Integer code) {
        if (code == null) {
            return CUSTOMER;
        }
        for (RoleEnum role : values()) {
            if (role.code == code) {
                return role;
            }
        }
        return CUSTOMER;
    }

}
