package cn.bugstack.types.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SplitsTest {

    @Test
    public void shouldExposeSplitSymbols() {
        assertEquals(",", Splits.SPLIT);
        assertEquals("_", Splits.UNDERLINE);
    }
}
