package cn.bugstack.domain.payment.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlipayNotifyTaskEntity {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 外部交易号
     */
    private String outTradeNo;

    /**
     * 内部交易号/流水号
     */
    private String tradeNo;

    /**
     * 订单类型
     * 对应数据库 order_type (tinyint)
     */
    private Integer orderType;

    /**
     * 任务状态
     * 对应数据库 task_status (tinyint)
     * 通常定义：0-待处理, 1-成功, 2-失败, 3-处理中
     */
    private Integer taskStatus;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 通知参数 JSON
     * 对应数据库 parameter_json (text)
     * 使用 @JsonProperty 确保 Java 驼峰命名能正确映射到数据库下划线字段（如果 ORM 框架未自动配置）
     */
    @JsonProperty("parameter_json")
    private String parameterJson;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}
