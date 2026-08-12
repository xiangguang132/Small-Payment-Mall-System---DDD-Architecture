package cn.bugstack.domain.groupbuy.service.trial.node;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialRequest;
import cn.bugstack.domain.groupbuy.service.trial.factory.DefaultActivityStrategyFactory;
import cn.bugstack.types.exception.AppException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ErrorNodeTest {

    @Test
    public void shouldThrowE0002WhenTrialContextMissing() throws Exception {
        try {
            new ErrorNode().doApply(
                    new GroupBuyTrialRequest(),
                    new DefaultActivityStrategyFactory.DynamicContext()
            );
            fail("配置缺失时应抛出 AppException");
        } catch (AppException e) {
            assertEquals(Integer.valueOf(10002), e.getCode());
            assertEquals("无拼团营销配置", e.getInfo());
        }
    }
}
