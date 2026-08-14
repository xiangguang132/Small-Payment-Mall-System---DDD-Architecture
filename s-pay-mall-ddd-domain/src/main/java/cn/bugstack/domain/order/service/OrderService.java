package cn.bugstack.domain.order.service;

import cn.bugstack.domain.order.adapter.port.IProductPort;
import cn.bugstack.domain.order.adapter.repository.IOrderRepository;
import cn.bugstack.domain.order.model.aggregate.CreateOrderAggregate;
import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.model.valobj.OrderStatusVO;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 该部分并不专注于实现业务，而是将有操作数据库或者http外部数据的
 * 进行下沉
 * 下沉到 Repository 接口层
 */

@Slf4j
@Service
public class OrderService extends AbstractOrderService{

    @org.springframework.beans.factory.annotation.Value("${alipay.notify_url}")
    private String notifyUrl;
    @Value("${alipay.return_url}")
    private String returnUrl;
    @Resource
    private AlipayClient alipayClient;

    public OrderService(IOrderRepository orderRepository, IProductPort productPort) {
        super(orderRepository, productPort);
    }

    @Override
    protected void doSaveOrder(CreateOrderAggregate orderAggregate) {
        orderRepository.doSaveOrder(orderAggregate);
    }

    @Override
    protected PayOrderEntity doPrepayOrder(String userId, String productId, String productName, String orderId, BigDecimal totalAmount) throws AlipayApiException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(returnUrl);
        request.setNotifyUrl(notifyUrl);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId);
        bizContent.put("total_amount", totalAmount.toString());
        bizContent.put("subject", productName);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());

        String form = alipayClient.pageExecute(request).getBody();

        PayOrderEntity payOrderEntity = new PayOrderEntity();
        payOrderEntity.setOrderId(orderId);
        payOrderEntity.setPayUrl(form);
        payOrderEntity.setOrderStatus(OrderStatusVO.PAY_WAIT);

        orderRepository.updateOrderPayInfo(payOrderEntity);

        return payOrderEntity;
    }

    @Override
    public void changeOrderPaySuccess(String orderId, Date outTradeTime) {
        orderRepository.changeOrderPaySuccess(orderId, outTradeTime);
    }

    @Override
    public List<String> queryNoPayNotifyOrderList() {
        return orderRepository.queryNoPayNotifyOrderList();
    }

    @Override
    public List<String> queryTimeOutCloseOrderList() {
        return orderRepository.queryTimeOutCloseOrderList();
    }

    @Override
    public boolean changeOrderPayClose(String orderId) {
        return orderRepository.changeOrderPayClose(orderId);
    }
}
