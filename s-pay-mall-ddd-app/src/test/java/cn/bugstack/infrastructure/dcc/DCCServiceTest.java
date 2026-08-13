package cn.bugstack.infrastructure.dcc;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DCCServiceTest {

    @Test
    public void shouldNotDowngradeWhenSwitchIsZero() {
        DCCService service = new DCCService();
        ReflectionTestUtils.setField(service, "downgradeSwitch", "0");

        assertFalse(service.isDowngradeSwitch());
    }

    @Test
    public void shouldDowngradeWhenSwitchIsOne() {
        DCCService service = new DCCService();
        ReflectionTestUtils.setField(service, "downgradeSwitch", "1");

        assertTrue(service.isDowngradeSwitch());
    }

    @Test
    public void shouldCutAllUsersWhenRangeIs100() {
        DCCService service = new DCCService();
        ReflectionTestUtils.setField(service, "cutRange", "100");

        assertTrue(service.isCutRange("10001"));
        assertTrue(service.isCutRange("xiaofuge"));
    }

    @Test
    public void shouldCutOnlyUsersWithinConfiguredRange() {
        DCCService service = new DCCService();
        ReflectionTestUtils.setField(service, "cutRange", "50");

        assertTrue(service.isCutRange("liergou"));
        assertFalse(service.isCutRange("gudebai"));
    }
}
