package cn.bugstack.trigger.interceptor;

import cn.bugstack.types.enums.RoleEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限注解，标注在 Controller 类或方法上，限制访问所需的角色。
 * 示例：@RequireRole({RoleEnum.ADMIN})
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /** 允许访问的角色列表（任一匹配即可） */
    RoleEnum[] value();

}
