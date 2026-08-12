package cn.bugstack.domain.groupbuy.service.trial.node;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyDiscountEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialRequest;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialResult;
import cn.bugstack.domain.groupbuy.service.trial.factory.DefaultActivityStrategyFactory;
import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EndNodeTest {

    private final EndNode endNode = new EndNode();

    @Test
    public void shouldBuildResultWithContextPrices() throws Exception {
        DefaultActivityStrategyFactory.DynamicContext context =
                context("100.00", "88.00", "12.00");

        GroupBuyTrialResult result =
                endNode.doApply(new GroupBuyTrialRequest(), context);

        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.getOriginalPrice());
        assertEquals(new BigDecimal("12.00"), result.getDeductionPrice());
        assertEquals(new BigDecimal("88.00"), result.getPayPrice());
    }

    @Test
    public void shouldFallbackToOriginalPriceWhenPricesNotSet() throws Exception {
        DefaultActivityStrategyFactory.DynamicContext context =
                context("100.00", null, null);

        GroupBuyTrialResult result =
                endNode.doApply(new GroupBuyTrialRequest(), context);

        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.getOriginalPrice());
        assertEquals(new BigDecimal("0.00"), result.getDeductionPrice());
        assertEquals(new BigDecimal("100.00"), result.getPayPrice());
    }

    private DefaultActivityStrategyFactory.DynamicContext context(
            String originalPrice,
            String payPrice,
            String deductionPrice
    ) {
        GroupBuyActivityEntity activity = GroupBuyActivityEntity.builder()
                .activityId(1L)
                .activityName("测试拼团")
                .productId(101L)
                .build();

        GroupBuyDiscountEntity discount = GroupBuyDiscountEntity.builder()
                .discountId("ZK001")
                .discountName("9折")
                .build();

        ProductAggregate product = ProductAggregate.builder()
                .id(101L)
                .name("测试商品")
                .price(new BigDecimal(originalPrice))
                .build();

        return DefaultActivityStrategyFactory.DynamicContext.builder()
                .activity(activity)
                .discount(discount)
                .product(product)
                .originalPrice(new BigDecimal(originalPrice))
                .payPrice(payPrice == null ? null : new BigDecimal(payPrice))
                .deductionPrice(deductionPrice == null ? null : new BigDecimal(deductionPrice))
                .build();
    }
}
