package cn.bugstack.domain.groupbuy.service.trial.node;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyDiscountEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialRequest;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialResult;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyActivityRepository;
import cn.bugstack.domain.groupbuy.service.trial.factory.DefaultActivityStrategyFactory;
import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import org.junit.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TagNodeTest {

    @Test
    public void shouldAllowActivityWithoutTagId() throws Exception {
        FakeGroupBuyActivityRepository repository = new FakeGroupBuyActivityRepository(false);
        TagNode tagNode = tagNode(repository);
        DefaultActivityStrategyFactory.DynamicContext context =
                context(null, null, "100.00");

        GroupBuyTrialResult result =
                tagNode.doApply(new GroupBuyTrialRequest(), context);

        assertNotNull(result);
        assertTrue(result.getVisible());
        assertTrue(result.getEnable());
        assertEquals(0, repository.withinTagCrowdRangeCalls);
    }

    @Test
    public void shouldAllowEveryoneWhenTagScopeIsBlank() throws Exception {
        assertTagResult("tag-001", null, false, true, true);
        assertTagResult("tag-001", "", false, true, true);
    }

    @Test
    public void shouldRestrictVisibilityWhenScopeOneAndUserNotInCrowd() throws Exception {
        assertTagResult("tag-001", "1", false, false, true);
    }

    @Test
    public void shouldAllowTaggedActivityWhenScopeOneAndUserInCrowd() throws Exception {
        assertTagResult("tag-001", "1", true, true, true);
    }

    @Test
    public void shouldRestrictEnableWhenScopeTwoAndUserNotInCrowd() throws Exception {
        assertTagResult("tag-001", "2", false, true, false);
    }

    @Test
    public void shouldAllowTaggedActivityWhenScopeTwoAndUserInCrowd() throws Exception {
        assertTagResult("tag-001", "2", true, true, true);
    }

    @Test
    public void shouldRestrictTaggedActivityWhenScopeOneAndTwoAndUserNotInCrowd() throws Exception {
        assertTagResult("tag-001", "1,2", false, false, false);
    }

    @Test
    public void shouldAllowTaggedActivityWhenScopeOneAndTwoAndUserInCrowd() throws Exception {
        assertTagResult("tag-001", " 1 , 2 ", true, true, true);
    }

    private void assertTagResult(
            String tagId,
            String tagScope,
            boolean withinCrowd,
            boolean expectedVisible,
            boolean expectedEnable
    ) throws Exception {
        TagNode tagNode = tagNode(new FakeGroupBuyActivityRepository(withinCrowd));
        DefaultActivityStrategyFactory.DynamicContext context =
                context(tagId, tagScope, "100.00");

        GroupBuyTrialResult result =
                tagNode.doApply(new GroupBuyTrialRequest(), context);

        assertNotNull(result);
        assertEquals(expectedVisible, result.getVisible());
        assertEquals(expectedEnable, result.getEnable());
    }

    private TagNode tagNode(FakeGroupBuyActivityRepository activityRepository) throws Exception {
        TagNode tagNode = new TagNode();
        Field endNodeField = TagNode.class.getDeclaredField("endNode");
        endNodeField.setAccessible(true);
        endNodeField.set(tagNode, new EndNode());

        Field activityRepositoryField = TagNode.class.getSuperclass().getDeclaredField("activityRepository");
        activityRepositoryField.setAccessible(true);
        activityRepositoryField.set(tagNode, activityRepository);

        return tagNode;
    }

    private DefaultActivityStrategyFactory.DynamicContext context(
            String tagId,
            String tagScope,
            String originalPrice
    ) {
        GroupBuyActivityEntity activity = GroupBuyActivityEntity.builder()
                .activityId(1L)
                .activityName("测试拼团")
                .productId(101L)
                .tagId(tagId)
                .tagScope(tagScope)
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

    private static class FakeGroupBuyActivityRepository implements IGroupBuyActivityRepository {

        private final boolean within;
        private int withinTagCrowdRangeCalls;

        private FakeGroupBuyActivityRepository(boolean within) {
            this.within = within;
        }

        @Override
        public GroupBuyActivityEntity queryGroupBuyActivityByActivityId(Long activityId) {
            return null;
        }

        @Override
        public GroupBuyActivityEntity queryGroupBuyActivityByProductId(Long productId) {
            return null;
        }

        @Override
        public java.util.List<GroupBuyActivityEntity> queryGroupBuyActivityByProductIds(java.util.List<Long> productIds) {
            return java.util.Collections.emptyList();
        }

        @Override
        public boolean withinTagCrowdRange(String tagId, String userId) {
            withinTagCrowdRangeCalls++;
            return within;
        }

        @Override
        public boolean downgradeSwitch() {
            return false;
        }

        @Override
        public boolean cutRange(String userId) {
            return true;
        }

        @Override
        public List<GroupBuyActivityEntity> queryActivityPageByMarketPlan(String marketPlan, int offset, int limit) {
            return java.util.Collections.emptyList();
        }

        @Override
        public long countActivityPageByMarketPlan(String marketPlan) {
            return 0;
        }
    }
}
