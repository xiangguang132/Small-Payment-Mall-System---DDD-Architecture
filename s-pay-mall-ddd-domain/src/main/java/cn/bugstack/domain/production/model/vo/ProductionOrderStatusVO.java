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
        return CREATED == status || RETRYABLE_FAILED == status;
    }

    public static boolean isTerminal(Integer status) {
        return COMPLETED == status || FINAL_FAILED == status || CANCELED == status;
    }
}
