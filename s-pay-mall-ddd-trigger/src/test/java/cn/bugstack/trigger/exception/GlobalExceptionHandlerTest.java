package cn.bugstack.trigger.exception;

import cn.bugstack.api.response.Response;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.junit.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    public void shouldMapAppExceptionToHttpStatus() {
        ResponseEntity<Response<Object>> response = handler.handleAppException(
                new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "bad input")
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals(ResponseCode.UNPROCESSABLE_ENTITY.getCode(), response.getBody().getCode());
        assertEquals("bad input", response.getBody().getInfo());
    }

    @Test
    public void shouldMapNotFoundAppException() {
        ResponseEntity<Response<Object>> response = handler.handleAppException(
                new AppException(ResponseCode.NOT_FOUND, "not found")
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(ResponseCode.NOT_FOUND.getCode(), response.getBody().getCode());
    }

    @Test
    public void shouldMapDuplicateKeyToConflict() {
        ResponseEntity<Response<Object>> response = handler.handleDuplicateKeyException(
                new DuplicateKeyException("duplicate")
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(ResponseCode.CONFLICT.getCode(), response.getBody().getCode());
    }

    @Test
    public void shouldMapGenericExceptionToInternalError() {
        ResponseEntity<Response<Object>> response = handler.handleException(new IllegalStateException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(ResponseCode.UN_ERROR.getCode(), response.getBody().getCode());
        assertNotNull(response.getBody().getInfo());
    }
}
