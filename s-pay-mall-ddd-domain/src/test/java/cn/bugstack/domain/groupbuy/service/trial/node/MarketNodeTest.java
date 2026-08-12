package cn.bugstack.domain.groupbuy.service.trial.node;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyDiscountEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialRequest;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialResult;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyActivityRepository;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyDiscountRepository;
import cn.bugstack.domain.groupbuy.service.discount.IGroupBuyDiscountService;
import cn.bugstack.domain.groupbuy.service.trial.factory.DefaultActivityStrategyFactory;
import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import cn.bugstack.domain.product.repository.IProductRepository;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 说明：
 * MarketNode 是试算规则树里最核心的“并发取数节点”：
 * 1. 并发查询活动、折扣、商品。
 * 2. 把查询结果写入动态上下文。
 * 3. 成功时把请求继续交给后续节点，失败时抛出业务异常或走错误节点。
 *
 * 这个测试类主要在回答两个问题：
 * 1. 正常情况下，MarketNode 是否能并发查齐数据并组装出完整试算结果。
 * 2. 缺少活动配置时，MarketNode 是否会立即按业务约定失败。
 */
public class MarketNodeTest {

    private static final Logger log = LoggerFactory.getLogger(MarketNodeTest.class);

    @Test
    public void shouldRunAsyncQueriesAndBuildTrialResult() throws Exception {
        log.info("开始测试 MarketNode，验证并发查询活动/折扣/商品后能否继续完成试算结果组装");

        ThreadPoolExecutor executor = executor();
        try {
            GroupBuyActivityEntity activity = GroupBuyActivityEntity.builder()
                    .activityId(1L)
                    .activityName("测试拼团")
                    .productId(101L)
                    .discountId("ZK001")
                    .build();

            GroupBuyDiscountEntity discount = GroupBuyDiscountEntity.builder()
                    .discountId("ZK001")
                    .discountName("9折")
                    .marketPlan("ZK")
                    .build();

            ProductAggregate product = ProductAggregate.builder()
                    .id(101L)
                    .name("测试商品")
                    .price(new BigDecimal("100.00"))
                    .build();

            MarketNode marketNode = marketNode(activity, discount, product);
            setField(marketNode, "threadPoolExecutor", executor);
            setField(marketNode, "discountServiceMap", discountServiceMap());

            EndNode endNode = new EndNode();
            TagNode tagNode = new TagNode();
            setField(tagNode, "endNode", endNode);
            setField(marketNode, "tagNode", tagNode);
            setField(marketNode, "errorNode", new ErrorNode());

            // 直接从 MarketNode 发起试算，相当于验证“并发查询 + 后继节点串联”整段链路。
            GroupBuyTrialResult result = marketNode.apply(
                    request(1L, 101L),
                    new DefaultActivityStrategyFactory.DynamicContext()
            );

            // 当前折扣服务还是空实现，所以这里验证的是数据查齐后能走通链路并返回基础试算结果。
            assertNotNull(result);
            assertEquals(Long.valueOf(1L), result.getActivityId());
            assertEquals(Long.valueOf(101L), result.getProductId());
            assertEquals("ZK001", result.getDiscountId());
            assertEquals(new BigDecimal("100.00"), result.getOriginalPrice());
            assertTrue(new BigDecimal("100.00").compareTo(result.getPayPrice()) == 0);
            assertTrue(new BigDecimal("0.00").compareTo(result.getDeductionPrice()) == 0);
            assertTrue(Boolean.TRUE.equals(result.getVisible()));
            assertTrue(Boolean.TRUE.equals(result.getEnable()));

            log.info("MarketNode 正常链路测试通过，activityId:{}, productId:{}, payPrice:{}",
                    result.getActivityId(), result.getProductId(), result.getPayPrice());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void shouldThrowWhenActivityNotFound() throws Exception {
        log.info("开始测试 MarketNode 异常链路，验证活动缺失时是否抛出 NOT_FOUND");

        ThreadPoolExecutor executor = executor();
        try {
            MarketNode marketNode = marketNode(null, null, product());
            setField(marketNode, "threadPoolExecutor", executor);

            try {
                marketNode.apply(
                        request(999L, 101L),
                        new DefaultActivityStrategyFactory.DynamicContext()
                );
                fail("活动缺失时应抛出 AppException");
            } catch (AppException e) {
                assertEquals(ResponseCode.NOT_FOUND.getCode(), e.getCode());
                log.info("MarketNode 缺失活动测试通过，异常码:{}", e.getCode());
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private MarketNode marketNode(
            GroupBuyActivityEntity activity,
            GroupBuyDiscountEntity discount,
            ProductAggregate product
    ) throws Exception {
        // 使用内存桩仓储隔离数据库依赖，让测试专注于 MarketNode 的并发编排逻辑。
        MarketNode marketNode = new MarketNode();
        setField(marketNode, "activityRepository", new IGroupBuyActivityRepository() {
            @Override
            public GroupBuyActivityEntity queryGroupBuyActivityByActivityId(Long activityId) {
                return activity;
            }

            @Override
            public boolean withinTagCrowdRange(String tagId, String userId) {
                return true;
            }
        });
        setField(marketNode, "discountRepository", (IGroupBuyDiscountRepository) discountId -> discount);
        setField(marketNode, "productRepository", productRepository(product));
        return marketNode;
    }

    private IProductRepository productRepository(ProductAggregate product) {
        return new IProductRepository() {
            @Override
            public Long save(ProductAggregate productAggregate) {
                return product == null ? null : product.getId();
            }

            @Override
            public void deleteById(Long id) {
            }

            @Override
            public ProductAggregate queryById(Long id) {
                return product;
            }

            @Override
            public void updateById(ProductAggregate updated) {
            }

            @Override
            public long countByCategoryId(Long categoryId) {
                return 0L;
            }
        };
    }

    private Map<String, IGroupBuyDiscountService> discountServiceMap() {
        Map<String, IGroupBuyDiscountService> map = new HashMap<>();
        map.put("ZK", (userId, originalPrice, discount) -> originalPrice);
        return map;
    }

    private ProductAggregate product() {
        return ProductAggregate.builder()
                .id(101L)
                .name("测试商品")
                .price(new BigDecimal("100.00"))
                .build();
    }

    private ThreadPoolExecutor executor() {
        // 固定大小线程池，满足当前三路查询中的并发调度需求。
        return new ThreadPoolExecutor(
                2,
                2,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(2)
        );
    }

    private GroupBuyTrialRequest request(Long activityId, Long productId) {
        // 构造最小试算请求，只保留当前链路真正依赖的字段。
        return GroupBuyTrialRequest.builder()
                .userId("xiaofuge")
                .activityId(activityId)
                .productId(productId)
                .build();
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        // 通过反射注入私有字段，避免为了测试改动生产代码结构。
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException ignored) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
