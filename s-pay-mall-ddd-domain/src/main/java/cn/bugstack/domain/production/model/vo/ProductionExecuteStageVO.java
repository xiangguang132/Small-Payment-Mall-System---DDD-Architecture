package cn.bugstack.domain.production.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductionExecuteStageVO {

    QUERY_ORDER("QUERY_ORDER", false),
    ACCEPT_ORDER("ACCEPT_ORDER", false),
    CREATE_ALLOCATION("CREATE_ALLOCATION", false),
    LOCK_MATERIAL("LOCK_MATERIAL", false),
    OUTBOUND_MATERIAL("OUTBOUND_MATERIAL", true),
    INBOUND_PRODUCT("INBOUND_PRODUCT", true),
    COMPLETE_ORDER("COMPLETE_ORDER", true);

    private final String code;
    private final boolean needManualIntervention;
}
