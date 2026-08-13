package cn.bugstack.types.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface DCCValue {

    /**
     * 配置的 Key 和默认值，格式为 "key:defaultValue"
     */
    String value() default "";

}
