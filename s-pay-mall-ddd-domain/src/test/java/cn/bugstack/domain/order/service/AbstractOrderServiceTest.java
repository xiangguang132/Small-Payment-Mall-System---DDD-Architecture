//package cn.bugstack.domain.order.service;
//
//import cn.bugstack.domain.order.adapter.port.IProductPort;
//import cn.bugstack.domain.order.adapter.repository.IOrderRepository;
//import cn.bugstack.domain.order.model.aggregate.CreateOrderAggregate;
//import cn.bugstack.domain.order.model.entity.OrderEntity;
//import cn.bugstack.domain.order.model.entity.PayOrderEntity;
//import cn.bugstack.domain.order.model.entity.ProductEntity;
//import cn.bugstack.domain.order.model.entity.ShopCartEntity;
//import cn.bugstack.domain.order.model.valobj.OrderStatusVO;
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.math.BigDecimal;
//import java.util.Date;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//public class AbstractOrderServiceTest {
//
//    @Mock
//    private IOrderRepository orderRepository;
//
//    @Mock
//    private IProductPort productPort;
//
//    private CreateOrderAggregate savedAggregate;
//
//    private AbstractOrderService orderService;
//
//    @Before
//    public void setUp() {
//        MockitoAnnotations.initMocks(this);
//        orderService = new AbstractOrderService(orderRepository, productPort) {
//            @Override
//            protected void doSaveOrder(CreateOrderAggregate orderAggregate) {
//                savedAggregate = orderAggregate;
//                orderRepository.doSaveOrder(orderAggregate);
//            }
//
//            @Override
//            protected PayOrderEntity doPrepayOrder(
//                    String userId,
//                    String productId,
//                    String productName,
//                    String outTradeNo,
//                    BigDecimal totalAmount
//            ) {
//                return PayOrderEntity.builder()
//                        .outTradeNo(outTradeNo)
//                        .payUrl("pay://" + productName)
//                        .orderStatus(OrderStatusVO.PAY_WAIT)
//                        .build();
//            }
//
//            @Override
//            public void changeOrderPaySuccess(String outTradeNo, Date outTradeTime) {
//            }
//
//            @Override
//            public java.util.List<String> queryNoPayNotifyOrderList() {
//                return null;
//            }
//
//            @Override
//            public java.util.List<String> queryTimeOutCloseOrderList() {
//                return null;
//            }
//
//            @Override
//            public boolean changeOrderPayClose(String outTradeNo) {
//                return false;
//            }
//        };
//    }
//
//    @Test
//    public void shouldReturnExistingPayWaitOrder() throws Exception {
//        ShopCartEntity cart = cart("u1", "P001");
//        when(orderRepository.queryUnPayOrder(cart)).thenReturn(
//                OrderEntity.builder()
//                        .outTradeNo("O001")
//                        .payUrl("pay://existing")
//                        .orderStatus(OrderStatusVO.PAY_WAIT)
//                        .build()
//        );
//
//        PayOrderEntity result = orderService.createOrder(cart);
//
//        assertEquals("O001", result.getOutTradeNo());
//        assertEquals("pay://existing", result.getPayUrl());
//    }
//
//    @Test
//    public void shouldCreatePayOrderForExistingCreateOrder() throws Exception {
//        ShopCartEntity cart = cart("u1", "P001");
//        when(orderRepository.queryUnPayOrder(cart)).thenReturn(
//                OrderEntity.builder()
//                        .outTradeNo("O002")
//                        .productName("demo")
//                        .totalAmount(new BigDecimal("88.00"))
//                        .orderStatus(OrderStatusVO.CREATE)
//                        .build()
//        );
//
//        PayOrderEntity result = orderService.createOrder(cart);
//
//        assertEquals("O002", result.getOutTradeNo());
//        assertEquals("pay://demo", result.getPayUrl());
//    }
//
//    @Test
//    public void shouldQueryProductAndSaveNewOrder() throws Exception {
//        ShopCartEntity cart = cart("u1", "P001");
//        when(orderRepository.queryUnPayOrder(cart)).thenReturn(null);
//        when(productPort.queryProductByProductId("P001")).thenReturn(
//                ProductEntity.builder()
//                        .productId("P001")
//                        .productName("demo")
//                        .price(new BigDecimal("9.90"))
//                        .build()
//        );
//
//        PayOrderEntity result = orderService.createOrder(cart);
//
//        assertNotNull(savedAggregate);
//        assertEquals("u1", savedAggregate.getUserId());
//        assertEquals("P001", savedAggregate.getProductEntity().getProductId());
//        assertNotNull(savedAggregate.getOrderEntity().getOutTradeNo());
//        assertEquals("pay://demo", result.getPayUrl());
//        verify(orderRepository).doSaveOrder(savedAggregate);
//    }
//
//    private ShopCartEntity cart(String userId, String productId) {
//        return ShopCartEntity.builder().userId(userId).productId(productId).build();
//    }
//}
