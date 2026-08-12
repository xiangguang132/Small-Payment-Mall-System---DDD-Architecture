package cn.bugstack.domain.common.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EnumCodeUtilsTest {

    @Test
    public void shouldReturnTrueWhenCodeMatchesEnumValue() {
        assertTrue(EnumCodeUtils.isValid(1, Sample.values(), Sample::getCode));
    }

    @Test
    public void shouldReturnFalseWhenCodeIsUnknownOrArgumentsMissing() {
        assertFalse(EnumCodeUtils.isValid(99, Sample.values(), Sample::getCode));
        assertFalse(EnumCodeUtils.isValid(null, Sample.values(), Sample::getCode));
        assertFalse(EnumCodeUtils.isValid(1, null, Sample::getCode));
        assertFalse(EnumCodeUtils.isValid(1, Sample.values(), null));
    }

    private enum Sample {
        ONE(1), TWO(2);

        private final Integer code;

        Sample(Integer code) {
            this.code = code;
        }

        public Integer getCode() {
            return code;
        }
    }
}
