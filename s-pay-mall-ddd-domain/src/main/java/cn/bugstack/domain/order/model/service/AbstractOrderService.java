package cn.bugstack.domain.order.model.service;

import cn.bugstack.domain.order.adapter.port.IProductPort;
import cn.bugstack.domain.order.adapter.repository.IOrderRepository;
import cn.bugstack.domain.order.model.aggregate.CreateOrderAggregate;
import cn.bugstack.domain.order.model.entity.OrderEntity;
import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.model.entity.ProductEntity;
import cn.bugstack.domain.order.model.entity.ShopCartEntity;
import cn.bugstack.domain.order.model.valobj.OrderStatusVO;
import com.alipay.api.AlipayApiException;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
public abstract class AbstractOrderService implements IOrderService {

    protected final IOrderRepository orderRepository;
    protected final IProductPort productPort;

    public AbstractOrderService(IOrderRepository orderRepository, IProductPort productPort) {
        this.orderRepository = orderRepository;
        this.productPort = productPort;
    }

    @Override
    public PayOrderEntity createOrder(ShopCartEntity shopCartEntity) throws Exception {

        // 1. 查询掉单和未支付订单
        OrderEntity unpaidOrderEntity = orderRepository.queryUnPayOrder(shopCartEntity);
        // 2. 如果存在未支付订单并且是payweit状态->即掉单
        if(unpaidOrderEntity != null && OrderStatusVO.PAY_WAIT.equals(unpaidOrderEntity.getOrderStatus())){
            log.info("创建订单-已存在未支付订单，userId:{}, productId:{}, orderId:{}", shopCartEntity.getUserId(), shopCartEntity.getProductId(), unpaidOrderEntity.getOrderId());
            return PayOrderEntity.builder()
                    .orderId(unpaidOrderEntity.getOrderId())
                    .payUrl(unpaidOrderEntity.getPayUrl())
                    .build();
        } else if (unpaidOrderEntity != null && OrderStatusVO.CREATE.equals(unpaidOrderEntity.getOrderStatus())) {
            log.info("创建订单-存在，存在未创建支付单订单，创建支付单开始 userId:{} productId:{} orderId:{}", shopCartEntity.getUserId(), shopCartEntity.getProductId(), unpaidOrderEntity.getOrderId());
            // 构建需要 userId productId productName orderId totalamount
            PayOrderEntity payOrderEntity = this.doPrepayOrder(
                    shopCartEntity.getUserId(),
                    shopCartEntity.getProductId(),
                    unpaidOrderEntity.getProductName(),
                    unpaidOrderEntity.getOrderId(),
                    unpaidOrderEntity.getTotalAmount()
            );
            return PayOrderEntity.builder()
                    .orderId(payOrderEntity.getOrderId())
                    .payUrl(payOrderEntity.getPayUrl())
                    .build();
        }

        // 3.查询商品-聚合订单
        ProductEntity productEntity = productPort.queryProductByProductId(shopCartEntity.getProductId());

        OrderEntity orderEntity = CreateOrderAggregate.buildOrderEntity(
                productEntity.getProductId(),
                productEntity.getProductName()
        );

        CreateOrderAggregate orderAggregate = CreateOrderAggregate.builder()
                .userId(shopCartEntity.getUserId())
                .productEntity(productEntity)
                .orderEntity(orderEntity)
                .build();

        // 4. 保存订单
        this.doSaveOrder(orderAggregate);

        // 5. 创建支付单
        PayOrderEntity payOrderEntity = this.doPrepayOrder(shopCartEntity.getUserId(), productEntity.getProductId(), productEntity.getProductName(), orderEntity.getOrderId(), productEntity.getPrice());

        return PayOrderEntity.builder()
                .orderId(payOrderEntity.getOrderId())
                .payUrl(payOrderEntity.getPayUrl())
                .build();
    }


    protected abstract void doSaveOrder(CreateOrderAggregate orderAggregate);

    protected abstract PayOrderEntity doPrepayOrder(String userId, String productId, String productName, String orderId, BigDecimal totalAmount) throws AlipayApiException;

}
