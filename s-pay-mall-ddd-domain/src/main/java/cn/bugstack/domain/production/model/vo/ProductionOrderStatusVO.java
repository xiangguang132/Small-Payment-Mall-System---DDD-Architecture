package cn.bugstack.domain.production.model.vo;

public final class ProductionOrderStatusVO {

    private ProductionOrderStatusVO() {
    }

    public static final int CREATED = 0;
    public static final int PROCESSING = 1;
    public static final int COMPLETED = 2;
    public static final int RETRYABLE_FAILED = 3;
    public static final int FINAL_FAILED = 4;
    public static final int CANCELED = 5;

    public static boolean canExecute(Integer status) {
        return Integer.valueOf(CREATED).equals(status) || Integer.valueOf(RETRYABLE_FAILED).equals(status);
    }

    public static boolean canManualExecute(Integer status) {
        return Integer.valueOf(CREATED).equals(status);
    }

    public static boolean canRetry(Integer status) {
        return Integer.valueOf(RETRYABLE_FAILED).equals(status);
    }

    public static boolean canCancel(Integer status) {
        return Integer.valueOf(CREATED).equals(status) || Integer.valueOf(RETRYABLE_FAILED).equals(status);
    }

    public static boolean isTerminal(Integer status) {
        return Integer.valueOf(COMPLETED).equals(status)
                || Integer.valueOf(FINAL_FAILED).equals(status)
                || Integer.valueOf(CANCELED).equals(status);
    }
}
