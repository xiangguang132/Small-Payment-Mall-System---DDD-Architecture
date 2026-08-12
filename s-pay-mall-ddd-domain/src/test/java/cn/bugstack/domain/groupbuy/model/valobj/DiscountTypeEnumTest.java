package cn.bugstack.domain.groupbuy.model.valobj;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DiscountTypeEnumTest {

    @Test
    public void shouldResolveKnownCodes() {
        assertEquals(DiscountTypeEnum.BASE, DiscountTypeEnum.get(0));
        assertEquals(DiscountTypeEnum.TAG, DiscountTypeEnum.get(1));
    }

    @Test(expected = RuntimeException.class)
    public void shouldRejectUnknownCode() {
        DiscountTypeEnum.get(99);
    }
}
