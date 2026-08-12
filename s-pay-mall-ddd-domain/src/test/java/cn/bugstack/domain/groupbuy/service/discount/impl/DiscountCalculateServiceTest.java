package cn.bugstack.domain.groupbuy.service.discount.impl;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyDiscountEntity;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class DiscountCalculateServiceTest {

    @Test
    public void shouldCalculateZkDiscount() {
        ZKCalculateService service = new ZKCalculateService();
        GroupBuyDiscountEntity discount = discount("0.90");

        assertBigDecimalEquals("90.00", service.calculate("u1", new BigDecimal("100.00"), discount));
    }

    @Test
    public void shouldCalculateZjDiscountWithMinimumPrice() {
        ZJCalculateService service = new ZJCalculateService();

        assertBigDecimalEquals("95.00", service.calculate("u1", new BigDecimal("100.00"), discount("5.00")));
        assertBigDecimalEquals("0.01", service.calculate("u1", new BigDecimal("0.50"), discount("5.00")));
    }

    @Test
    public void shouldCalculateMjDiscountOnlyAboveThreshold() {
        MJCalculateService service = new MJCalculateService();
        GroupBuyDiscountEntity discount = discount("10,2");

        assertBigDecimalEquals("8.00", service.calculate("u1", new BigDecimal("10.00"), discount));
        assertBigDecimalEquals("9.00", service.calculate("u1", new BigDecimal("9.00"), discount));
        assertBigDecimalEquals("1.00", service.calculate("u1", new BigDecimal("1.00"), discount("10,2")));
        assertBigDecimalEquals("0.01", service.calculate("u1", new BigDecimal("10.00"), discount("10,20")));
    }

    @Test
    public void shouldUseNPriceAsReplacementPrice() {
        NCalculateService service = new NCalculateService();

        assertBigDecimalEquals("9.90", service.calculate("u1", new BigDecimal("100.00"), discount("9.90")));
    }

    private void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

    private GroupBuyDiscountEntity discount(String marketExpr) {
        return GroupBuyDiscountEntity.builder()
                .discountType(0)
                .marketExpr(marketExpr)
                .build();
    }
}
