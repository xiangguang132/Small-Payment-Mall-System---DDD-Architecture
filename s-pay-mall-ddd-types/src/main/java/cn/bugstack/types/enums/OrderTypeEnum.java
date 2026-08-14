package cn.bugstack.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderTypeEnum {

    DIRECT("DIRECT", "Direct purchase"),
    GROUP_BUY("GROUP_BUY", "Group buy"),
    ;

    private final String code;
    private final String desc;

}
