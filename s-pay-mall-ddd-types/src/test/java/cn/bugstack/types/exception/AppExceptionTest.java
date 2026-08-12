package cn.bugstack.types.exception;

import cn.bugstack.types.enums.ResponseCode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class AppExceptionTest {

    @Test
    public void shouldBuildFromResponseCode() {
        AppException exception = new AppException(ResponseCode.NOT_FOUND);

        assertEquals(Integer.valueOf(404), exception.getCode());
        assertEquals("资源不存在", exception.getInfo());
    }

    @Test
    public void shouldBuildFromCodeAndMessage() {
        AppException exception = new AppException(422, "bad input");

        assertEquals(Integer.valueOf(422), exception.getCode());
        assertEquals("bad input", exception.getInfo());
        assertEquals("bad input", exception.getMessage());
    }

    @Test
    public void shouldKeepCause() {
        IllegalStateException cause = new IllegalStateException("boom");
        AppException exception = new AppException(500, "failed", cause);

        assertSame(cause, exception.getCause());
        assertEquals("failed", exception.getMessage());
    }
}
