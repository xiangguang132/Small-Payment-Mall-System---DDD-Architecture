package cn.bugstack.domain.common.util;

import java.util.Objects;
import java.util.function.Function;

public class EnumCodeUtils {

    private EnumCodeUtils() {
    }

    public static <E extends Enum<E>> boolean isValid(Integer code, E[] values, Function<E, Integer> codeGetter) {
        if (code == null || values == null || codeGetter == null) {
            return false;
        }
        for (E value : values) {
            if (Objects.equals(code, codeGetter.apply(value))) {
                return true;
            }
        }
        return false;
    }
}
