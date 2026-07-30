package cn.bugstack.trigger.exception;

import cn.bugstack.api.response.Response;
import cn.bugstack.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Response<Object> response = Response.<Object>builder()
                .code(ResponseCode.UNPROCESSABLE_ENTITY.getCode())
                .info(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class})
    public ResponseEntity<Response<Object>> handleValidationException(Exception ex) {
        String message = ex.getMessage();
        if (ex instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException e = (MethodArgumentNotValidException) ex;
            if (e.getBindingResult().hasFieldErrors()) {
                message = e.getBindingResult().getFieldError().getDefaultMessage();
            }
        } else if (ex instanceof BindException) {
            BindException e = (BindException) ex;
            if (e.getBindingResult().hasFieldErrors()) {
                message = e.getBindingResult().getFieldError().getDefaultMessage();
            }
        } else if (ex instanceof ConstraintViolationException) {
            ConstraintViolationException e = (ConstraintViolationException) ex;
            if (!e.getConstraintViolations().isEmpty()) {
                message = e.getConstraintViolations().iterator().next().getMessage();
            }
        }
        Response<Object> response = Response.<Object>builder()
                .code(ResponseCode.UNPROCESSABLE_ENTITY.getCode())
                .info(message)
                .build();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Object>> handleException(Exception ex) {
        log.error("系统异常", ex);
        Response<Object> response = Response.<Object>builder()
                .code(ResponseCode.UN_ERROR.getCode())
                .info(ResponseCode.UN_ERROR.getInfo())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
