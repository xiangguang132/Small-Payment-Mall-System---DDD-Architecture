package cn.bugstack.domain.groupbuy.service.trial.rule;

import cn.bugstack.domain.groupbuy.model.entity.CouponEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyDiscountEntity;
import cn.bugstack.domain.groupbuy.model.entity.PointsEntity;
import cn.bugstack.domain.groupbuy.model.entity.TrialRuleResult;
import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrialRuleContext {

    /** 用户ID */
    private String userId;

    /** 商品ID */
    private Long productId;

    /** 拼团活动，允许为空 */
    private GroupBuyActivityEntity activity;

    /** 拼团折扣，允许为空 */
    private GroupBuyDiscountEntity discount;

    /** 商品聚合体 */
    private ProductAggregate product;

    /** 商品原价 */
    private BigDecimal originalPrice;

    /** 当前试算价格 */
    private BigDecimal currentPrice;

    /** 用户选择的券ID列表 */
    @Builder.Default
    private List<String> selectedCouponIds = new ArrayList<>();

    /** 已加载的券列表 */
    @Builder.Default
    private List<CouponEntity> selectedCoupons = new ArrayList<>();

    /** 用户积分账户 */
    private PointsEntity points;

    /** 已应用规则结果 */
    @Builder.Default
    private List<TrialRuleResult> appliedRuleResults = new ArrayList<>();

    public void apply(TrialRuleResult result) {
        if (result == null) {
            return;
        }
        if (appliedRuleResults == null) {
            appliedRuleResults = new ArrayList<>();
        }
        appliedRuleResults.add(result);
        if (result.getPayPrice() != null) {
            this.currentPrice = result.getPayPrice();
        }
        if (this.originalPrice == null) {
            this.originalPrice = result.getOriginalPrice();
        }
    }
}