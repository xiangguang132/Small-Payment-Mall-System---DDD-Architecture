package cn.bugstack.infrastructure.dao.payment;

import cn.bugstack.Application;
import cn.bugstack.infrastructure.dao.IAlipayNotifyTaskDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RBucket;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = Application.class,
        properties = {
                "spring.autoconfigure.exclude=" +
                        "org.redisson.spring.starter.RedissonAutoConfiguration," +
                        "org.redisson.spring.starter.RedissonAutoConfigurationV2"
        }
)
@Transactional
@Import(AlipayNotifyTaskDaoTest.RedissonTestConfig.class)
public class AlipayNotifyTaskDaoTest {

    @Autowired
    private IAlipayNotifyTaskDao alipayNotifyTaskDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String insertTask(String outTradeNo, int taskStatus, int retryCount) {
        jdbcTemplate.update(
                "insert into alipay_notify_task(" +
                        "out_trade_no, trade_no, order_type, task_status, retry_count, parameter_json, create_time, update_time) " +
                        "values (?, ?, ?, ?, ?, ?, now(), now())",
                outTradeNo,
                "trade-" + outTradeNo,
                0,
                taskStatus,
                retryCount,
                "{\"out_trade_no\":\"" + outTradeNo + "\"}"
        );
        return outTradeNo;
    }

    @Test
    public void shouldReturnPendingAndRetryTasksWithinRetryLimit() {
        String pending = insertTask("PENDING_" + System.nanoTime(), 0, 0);
        String retryable = insertTask("RETRY_" + System.nanoTime(), 2, 3);
        String success = insertTask("SUCCESS_" + System.nanoTime(), 1, 0);
        String dead = insertTask("DEAD_" + System.nanoTime(), 3, 5);
        String overLimit = insertTask("OVERLIMIT_" + System.nanoTime(), 0, 5);

        List<String> outTradeNos = alipayNotifyTaskDao.queryRetryOutTradeNoList();

        assertNotNull(outTradeNos);
        // 待处理(status=0) 与 重试(status=2) 且 retry_count<5 应被重投
        assertTrue(outTradeNos.contains(pending));
        assertTrue(outTradeNos.contains(retryable));
        // 成功(status=1)、失败(status=3)、retry_count 达上限 5 的不应被重投
        assertTrue(!outTradeNos.contains(success));
        assertTrue(!outTradeNos.contains(dead));
        assertTrue(!outTradeNos.contains(overLimit));
    }

    @Test
    public void shouldNotRepublishSuccessDeadOrOverLimitTasks() {
        String success = insertTask("SUCCESS_ONLY_" + System.nanoTime(), 1, 0);
        String dead = insertTask("DEAD_ONLY_" + System.nanoTime(), 3, 5);
        String overLimit = insertTask("OVERLIMIT_ONLY_" + System.nanoTime(), 2, 5);

        List<String> outTradeNos = alipayNotifyTaskDao.queryRetryOutTradeNoList();

        assertNotNull(outTradeNos);
        assertTrue(!outTradeNos.contains(success));
        assertTrue(!outTradeNos.contains(dead));
        assertTrue(!outTradeNos.contains(overLimit));
    }

    @TestConfiguration
    static class RedissonTestConfig {

        @Bean
        RedissonClient redissonClient() {
            RedissonClient redissonClient = mock(RedissonClient.class);
            RBucket<Object> downgradeBucket = mock(RBucket.class);
            when(downgradeBucket.isExists()).thenReturn(false);
            when(redissonClient.getBucket("group_buy_market_dcc_downgradeSwitch")).thenReturn(downgradeBucket);

            RBucket<Object> cutRangeBucket = mock(RBucket.class);
            when(cutRangeBucket.isExists()).thenReturn(false);
            when(redissonClient.getBucket("group_buy_market_dcc_cutRange")).thenReturn(cutRangeBucket);

            when(redissonClient.getTopic("group_buy_market_dcc")).thenReturn(mock(RTopic.class));
            return redissonClient;
        }
    }
}
