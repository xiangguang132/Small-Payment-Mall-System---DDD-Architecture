package cn.bugstack.config;

import cn.bugstack.types.annotations.DCCValue;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.redisson.api.RBucket;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class DCCValueBeanFactoryTest {

    static class DccBean {
        @DCCValue("testSwitch:0")
        String testSwitch;
    }

    static class InvalidDccBean {
        @DCCValue("missing")
        String testSwitch;
    }

    @Test
    public void shouldInjectDefaultValueIntoDccAnnotatedField() {
        RedissonClient client = mock(RedissonClient.class);
        RBucket<Object> bucket = mock(RBucket.class);
        when(client.getBucket("group_buy_market_dcc_testSwitch")).thenReturn(bucket);
        when(bucket.isExists()).thenReturn(false);

        DCCValueBeanFactory factory = new DCCValueBeanFactory(client);
        DccBean bean = new DccBean();

        Object result = factory.postProcessAfterInitialization(bean, "dccBean");

        assertSame(bean, result);
        assertEquals("0", bean.testSwitch);
        verify(bucket).set("0");
    }

    @Test
    public void shouldUpdateDccAnnotatedFieldWhenTopicMessageArrives() {
        RedissonClient client = mock(RedissonClient.class);
        RBucket<Object> bucket = mock(RBucket.class);
        when(client.getBucket("group_buy_market_dcc_testSwitch")).thenReturn(bucket);
        when(bucket.isExists()).thenReturn(false, true);

        RTopic topic = mock(RTopic.class);
        when(client.getTopic("group_buy_market_dcc")).thenReturn(topic);

        DCCValueBeanFactory factory = new DCCValueBeanFactory(client);
        DccBean bean = new DccBean();
        factory.postProcessAfterInitialization(bean, "dccBean");
        factory.testRedisTopicListener(client);

        ArgumentCaptor<MessageListener<String>> captor = ArgumentCaptor.forClass(MessageListener.class);
        verify(topic).addListener(eq(String.class), captor.capture());

        captor.getValue().onMessage("group_buy_market_dcc", "testSwitch,1");

        assertEquals("1", bean.testSwitch);
    }

    @Test(expected = RuntimeException.class)
    public void shouldRejectDccValueWithoutDefault() {
        RedissonClient client = mock(RedissonClient.class);
        DCCValueBeanFactory factory = new DCCValueBeanFactory(client);

        factory.postProcessAfterInitialization(new InvalidDccBean(), "invalidDccBean");
    }
}
