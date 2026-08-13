package cn.bugstack.infrastructure.dao.po.groupbuy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyActivity {

    /** 自增ID */
    private Long id;

    /** 活动ID */
    private Long activityId;

    /** 活动名称 */
    private String activityName;

    /** 商品ID，关联product.id */
    private Long productId;

    /** 折扣ID，关联group_buy_discount.discount_id */
    private String discountId;

    /** 拼团方式；0自动成团、1达成目标拼团 */
    private Integer groupType;

    /** 单用户参团次数限制 */
    private Integer takeLimitCount;

    /** 拼团目标人数 */
    private Integer targetCount;

    /** 拼团时长（分钟） */
    private Integer validTime;

    /** 活动状态；0创建、1生效、2过期、3废弃 */
    private Integer status;

    /** 活动开始时间 */
    private LocalDateTime startTime;

    /** 活动结束时间 */
    private LocalDateTime endTime;

    /** 人群标签ID */
    private String tagId;

    /** 人群标签范围；1可见限制、2参与限制 */
    private String tagScope;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
