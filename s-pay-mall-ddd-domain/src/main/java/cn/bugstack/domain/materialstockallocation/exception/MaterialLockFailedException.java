package cn.bugstack.domain.materialstockallocation.exception;

public class MaterialLockFailedException extends RuntimeException {

    public MaterialLockFailedException(String message) {
        super(message);
    }

    public MaterialLockFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
