package cn.bugstack.domain.groupbuy.service.trial.node;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyDiscountEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialRequest;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialResult;
import cn.bugstack.domain.groupbuy.service.trial.factory.DefaultActivityStrategyFactory;
import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import org.junit.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TagNodeTest {

    @Test
    public void shouldAllowActivityWithoutTagId() throws Exception {
        TagNode tagNode = tagNode();
        DefaultActivityStrategyFactory.DynamicContext context =
                context(null, "100.00");

        GroupBuyTrialResult result =
                tagNode.doApply(new GroupBuyTrialRequest(), context);

        assertNotNull(result);
        assertTrue(result.getVisible());
        assertTrue(result.getEnable());
    }

    @Test
    public void shouldKeepTaggedActivityClosedBeforeCrowdTagService() throws Exception {
        TagNode tagNode = tagNode();
        DefaultActivityStrategyFactory.DynamicContext context =
                context("tag-001", "100.00");

        GroupBuyTrialResult result =
                tagNode.doApply(new GroupBuyTrialRequest(), context);

        assertNotNull(result);
        assertFalse(result.getVisible());
        assertFalse(result.getEnable());
    }

    private TagNode tagNode() throws Exception {
        TagNode tagNode = new TagNode();
        Field endNodeField = TagNode.class.getDeclaredField("endNode");
        endNodeField.setAccessible(true);
        endNodeField.set(tagNode, new EndNode());
        return tagNode;
    }

    private DefaultActivityStrategyFactory.DynamicContext context(
            String tagId,
            String originalPrice
    ) {
        GroupBuyActivityEntity activity = GroupBuyActivityEntity.builder()
                .activityId(1L)
                .activityName("测试拼团")
                .productId(101L)
                .tagId(tagId)
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
                .build();
    }
}
