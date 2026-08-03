package cn.bugstack.domain.production.service;

import cn.bugstack.domain.production.model.vo.ProductionExecuteStageVO;
import lombok.Getter;

@Getter
public class ProductionExecuteException extends RuntimeException {

    private final ProductionExecuteStageVO stage;

    public ProductionExecuteException(ProductionExecuteStageVO stage, String message) {
        super(message);
        this.stage = stage;
    }

    public ProductionExecuteException(ProductionExecuteStageVO stage, Throwable cause) {
        super(cause == null ? null : cause.getMessage(), cause);
        this.stage = stage;
    }

    public String getFailStage() {
        return stage == null ? null : stage.getCode();
    }

    public Integer getNeedManualIntervention() {
        return stage != null && stage.isNeedManualIntervention() ? 1 : 0;
    }
}
