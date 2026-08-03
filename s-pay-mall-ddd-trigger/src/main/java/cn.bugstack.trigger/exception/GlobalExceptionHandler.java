package cn.bugstack.trigger.exception;

import cn.bugstack.api.response.Response;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<Response<Object>> handleAppException(AppException ex) {
        Response<Object> response = Response.<Object>builder()
                .code(ex.getCode())
                .info(ex.getInfo())
                .build();
        HttpStatus httpStatus = ResponseCode.CONFLICT.getCode().equals(ex.getCode())
                ? HttpStatus.CONFLICT
                : HttpStatus.OK;
        return ResponseEntity.status(httpStatus).body(response);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Response<Object>> handleDuplicateKeyException(DuplicateKeyException ex) {
        log.warn("数据唯一约束冲突", ex);
        Response<Object> response = Response.<Object>builder()
                .code(ResponseCode.CONFLICT.getCode())
                .info("数据已存在，请勿重复提交")
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<Response<Object>> handleBadRequestException(Exception ex) {
        Response<Object> response = Response.<Object>builder()
                .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                .info("请求参数格式错误")
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Response<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        Response<Object> response = Response.<Object>builder()
                .code(ResponseCode.METHOD_NOT_ALLOWED.getCode())
                .info(ResponseCode.METHOD_NOT_ALLOWED.getInfo())
                .build();
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

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
