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
    public void shouldReturnOnlyPendingTasks() {
        String pendingA = insertTask("PENDING_A_" + System.nanoTime(), 0, 0);
        String pendingB = insertTask("PENDING_B_" + System.nanoTime(), 0, 3);
        String success = insertTask("SUCCESS_" + System.nanoTime(), 1, 0);
        String failed = insertTask("FAILED_" + System.nanoTime(), 2, 5);

        List<String> outTradeNos = alipayNotifyTaskDao.queryRetryOutTradeNoList();

        assertNotNull(outTradeNos);
        assertTrue(outTradeNos.contains(pendingA));
        assertTrue(outTradeNos.contains(pendingB));
        // 已成功 / 已失败（status 1/2）的任务不应被重新投递
        assertTrue(!outTradeNos.contains(success));
        assertTrue(!outTradeNos.contains(failed));
    }

    @Test
    public void shouldReturnEmptyWhenNoPendingTask() {
        insertTask("SUCCESS_ONLY_" + System.nanoTime(), 1, 0);
        insertTask("FAILED_ONLY_" + System.nanoTime(), 2, 5);

        List<String> outTradeNos = alipayNotifyTaskDao.queryRetryOutTradeNoList();

        assertNotNull(outTradeNos);
        assertTrue(outTradeNos.isEmpty());
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
