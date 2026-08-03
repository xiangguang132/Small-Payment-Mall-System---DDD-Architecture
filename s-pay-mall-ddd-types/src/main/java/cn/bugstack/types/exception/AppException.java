package cn.bugstack.types.exception;

import cn.bugstack.types.enums.ResponseCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义异常类
 * 业务代码里主动抛的异常类型
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppException extends RuntimeException {

    private static final long serialVersionUID = 8653090271840061986L;

    /**
     * 异常码
     */
    private Integer code;

    /**
     * 异常信息
     */
    private String info;

    public AppException(Integer code) {
        this.code = code;
    }

    public AppException(Integer code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public AppException(Integer code, String message) {
        super(message);
        this.code = code;
        this.info = message;
    }

    public AppException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.info = message;
    }

    public AppException(ResponseCode responseCode) {
        super(responseCode.getInfo());
        this.code = responseCode.getCode();
        this.info = responseCode.getInfo();
    }

    public AppException(ResponseCode responseCode, String message) {
        super(message);
        this.code = responseCode.getCode();
        this.info = message;
    }

    public AppException(ResponseCode responseCode, String message, Throwable cause) {
        super(message, cause);
        this.code = responseCode.getCode();
        this.info = message;
    }

    @Override
    public String toString() {
        return "cn.bugstack.x.api.types.exception.AppException{" +
                "code='" + code + '\'' +
                ", info='" + info + '\'' +
                '}';
    }

}
