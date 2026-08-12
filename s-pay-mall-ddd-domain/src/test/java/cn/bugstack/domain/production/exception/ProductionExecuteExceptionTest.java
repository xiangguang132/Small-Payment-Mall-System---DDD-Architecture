package cn.bugstack.domain.production.exception;

import cn.bugstack.domain.production.model.vo.ProductionExecuteStageVO;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ProductionExecuteExceptionTest {

    @Test
    public void shouldExposeStageAndMessage() {
        ProductionExecuteException exception =
                new ProductionExecuteException(ProductionExecuteStageVO.INBOUND_PRODUCT, "inbound failed");

        assertEquals(ProductionExecuteStageVO.INBOUND_PRODUCT, exception.getStage());
        assertEquals("INBOUND_PRODUCT", exception.getFailStage());
        assertEquals(Integer.valueOf(1), exception.getNeedManualIntervention());
    }

    @Test
    public void shouldBuildFromCauseAndHandleNullStage() {
        IllegalStateException cause = new IllegalStateException("cause");
        ProductionExecuteException exception =
                new ProductionExecuteException(ProductionExecuteStageVO.ACCEPT_ORDER, cause);
        ProductionExecuteException nullStage =
                new ProductionExecuteException(null, "no stage");

        assertEquals("cause", exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertNull(nullStage.getFailStage());
        assertEquals(Integer.valueOf(0), nullStage.getNeedManualIntervention());
    }
}
