package cn.bugstack.domain.supplier.model.vo;

public class SupplierStatusVO {

    public static final int DISABLED = 0;
    public static final int ENABLED = 1;

    private SupplierStatusVO() {
    }

    public static boolean isValid(Integer status) {
        return status != null && (status == DISABLED || status == ENABLED);
    }
}
