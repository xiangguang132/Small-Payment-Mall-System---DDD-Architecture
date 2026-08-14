package cn.bugstack.infrastructure.adapter.repository;

import cn.bugstack.domain.order.event.PaySuccessMessageEvent;
import cn.bugstack.domain.order.model.aggregate.CreateOrderAggregate;
import cn.bugstack.domain.order.model.entity.OrderEntity;
import cn.bugstack.domain.order.model.entity.ProductEntity;
import cn.bugstack.domain.order.model.valobj.OrderStatusVO;
import cn.bugstack.infrastructure.dao.IOrderDao;
import cn.bugstack.infrastructure.dao.po.payment.PayOrder;
import cn.bugstack.types.enums.OrderTypeEnum;
import cn.bugstack.types.event.BaseEvent;
import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderRepositoryTest {

    @Mock
    private IOrderDao orderDao;
    @Mock
    private EventBus eventBus;
    @Mock
    private PaySuccessMessageEvent paySuccessMessageEvent;
    @InjectMocks
    private OrderRepository orderRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldSaveDirectOrderTypeWhenSavingOrder() {
        CreateOrderAggregate aggregate = CreateOrderAggregate.builder()
                .userId("u1")
                .productEntity(ProductEntity.builder()
                        .productId("P001")
                        .productName("demo")
                        .price(new BigDecimal("9.90"))
                        .build())
                .orderEntity(OrderEntity.builder()
                        .orderId("O001")
                        .orderTime(new Date())
                        .orderStatus(OrderStatusVO.CREATE)
                        .build())
                .build();

        orderRepository.doSaveOrder(aggregate);

        ArgumentCaptor<PayOrder> captor = ArgumentCaptor.forClass(PayOrder.class);
        verify(orderDao).insert(captor.capture());
        assertEquals(OrderTypeEnum.DIRECT.getCode(), captor.getValue().getOrderType());
    }

    @Test
    public void shouldPassOutTradeTimeWhenOrderPaySuccess() {
        Date outTradeTime = new Date();
        when(paySuccessMessageEvent.buildEventMessage(any(PaySuccessMessageEvent.PaySuccessMessage.class)))
                .thenReturn(BaseEvent.EventMessage.<PaySuccessMessageEvent.PaySuccessMessage>builder()
                        .id("1")
                        .timestamp(new Date())
                        .data(PaySuccessMessageEvent.PaySuccessMessage.builder()
                                .tradeNo("O001")
                                .build())
                        .build());

        orderRepository.changeOrderPaySuccess("O001", outTradeTime);

        ArgumentCaptor<PayOrder> captor = ArgumentCaptor.forClass(PayOrder.class);
        verify(orderDao).changeOrderPaySuccess(captor.capture());
        assertEquals(OrderStatusVO.PAY_SUCCESS.getCode(), captor.getValue().getStatus());
        assertSame(outTradeTime, captor.getValue().getOutTradeTime());
    }
}
