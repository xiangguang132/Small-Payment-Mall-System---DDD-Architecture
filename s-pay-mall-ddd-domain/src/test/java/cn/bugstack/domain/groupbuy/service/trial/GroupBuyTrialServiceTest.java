package cn.bugstack.domain.groupbuy.service.trial;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialRequest;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialResult;
import cn.bugstack.domain.groupbuy.service.trial.factory.DefaultActivityStrategyFactory;
import cn.bugstack.types.design.framework.tree.StrategyHandler;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GroupBuyTrialServiceTest {

    @Test
    public void shouldApplyTreeHandlerAndReturnTrialResult() throws Exception {
        DefaultActivityStrategyFactory factory = new DefaultActivityStrategyFactory(null) {
            @Override
            public StrategyHandler<
                    GroupBuyTrialRequest,
                    DefaultActivityStrategyFactory.DynamicContext,
                    GroupBuyTrialResult
                    > strategyHandler() {
                return (request, dynamicContext) -> GroupBuyTrialResult.builder()
                        .activityId(1L)
                        .payPrice(new BigDecimal("88.00"))
                        .build();
            }
        };

        GroupBuyTrialService service = new GroupBuyTrialService(factory);

        GroupBuyTrialResult result =
                service.queryGroupBuyTrial(new GroupBuyTrialRequest());

        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getActivityId());
        assertEquals(new BigDecimal("88.00"), result.getPayPrice());
    }
}
