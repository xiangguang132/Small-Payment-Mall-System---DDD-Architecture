package cn.bugstack.domain.groupbuy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyTrialRequest {

    /** 用户ID */
    private String userId;

    /** 拼团活动ID */
    private Long activityId;

    /** 商品ID，用于和活动配置中的商品做一致性校验 */
    private Long productId;

}
