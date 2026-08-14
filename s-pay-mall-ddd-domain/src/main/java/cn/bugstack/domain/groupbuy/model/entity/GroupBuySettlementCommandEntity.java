package cn.bugstack.domain.groupbuy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 结算-命令实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuySettlementCommandEntity {

    private String userId;
    private String outTradeNo;
    private LocalDateTime payTime;
    private String source;
    private String channel;

}
