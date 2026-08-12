package cn.bugstack.domain.production.model.vo;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProductionOrderStatusVOTest {

    @Test
    public void shouldAllowExecuteForCreatedAndRetryableOnly() {
        assertTrue(ProductionOrderStatusVO.canExecute(ProductionOrderStatusVO.CREATED));
        assertTrue(ProductionOrderStatusVO.canExecute(ProductionOrderStatusVO.RETRYABLE_FAILED));
        assertFalse(ProductionOrderStatusVO.canExecute(ProductionOrderStatusVO.PROCESSING));
        assertFalse(ProductionOrderStatusVO.canExecute(null));
    }

    @Test
    public void shouldAllowManualExecuteAndRetryByStatus() {
        assertTrue(ProductionOrderStatusVO.canManualExecute(ProductionOrderStatusVO.CREATED));
        assertFalse(ProductionOrderStatusVO.canManualExecute(ProductionOrderStatusVO.RETRYABLE_FAILED));
        assertTrue(ProductionOrderStatusVO.canRetry(ProductionOrderStatusVO.RETRYABLE_FAILED));
        assertFalse(ProductionOrderStatusVO.canRetry(ProductionOrderStatusVO.CREATED));
    }

    @Test
    public void shouldAllowCancelForNonTerminalCreatedAndRetryable() {
        assertTrue(ProductionOrderStatusVO.canCancel(ProductionOrderStatusVO.CREATED));
        assertTrue(ProductionOrderStatusVO.canCancel(ProductionOrderStatusVO.RETRYABLE_FAILED));
        assertFalse(ProductionOrderStatusVO.canCancel(ProductionOrderStatusVO.COMPLETED));
    }

    @Test
    public void shouldDetectTerminalStatuses() {
        assertTrue(ProductionOrderStatusVO.isTerminal(ProductionOrderStatusVO.COMPLETED));
        assertTrue(ProductionOrderStatusVO.isTerminal(ProductionOrderStatusVO.FINAL_FAILED));
        assertTrue(ProductionOrderStatusVO.isTerminal(ProductionOrderStatusVO.CANCELED));
        assertFalse(ProductionOrderStatusVO.isTerminal(ProductionOrderStatusVO.CREATED));
    }
}
