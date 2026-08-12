package cn.bugstack.domain.order.event;

import cn.bugstack.types.event.BaseEvent;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PaySuccessMessageEventTest {

    @Test
    public void shouldBuildEventMessageAndTopic() {
        PaySuccessMessageEvent event = new PaySuccessMessageEvent();
        PaySuccessMessageEvent.PaySuccessMessage data =
                PaySuccessMessageEvent.PaySuccessMessage.builder()
                        .userId("u1")
                        .tradeNo("T001")
                        .build();

        BaseEvent.EventMessage<PaySuccessMessageEvent.PaySuccessMessage> message =
                event.buildEventMessage(data);

        assertEquals("pay_success", event.topic());
        assertNotNull(message.getId());
        assertNotNull(message.getTimestamp());
        assertEquals(data, message.getData());
    }
}
