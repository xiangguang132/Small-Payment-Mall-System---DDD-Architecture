package cn.bugstack.api;

import cn.bugstack.api.request.trade.ConfirmOrderRequest;
import cn.bugstack.api.request.trade.LockOrderRequest;
import cn.bugstack.api.request.trade.RefundOrderRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.trade.ConfirmOrderResponse;
import cn.bugstack.api.response.trade.LockOrderResponse;

/**
 * IPayService 放在 api 层就是为了方便其他后端项目调用，这是 DDD 分层架构中常见的设计模式
 *   - 方便其他微服务调用
 *   - 统一管理对外暴露的接口
 *   - 为将来微服务拆分做准备
 */
public interface IPayService {

    Response<LockOrderResponse> lockOrder(LockOrderRequest request);

    Response<ConfirmOrderResponse> confirmOrder(ConfirmOrderRequest request);

    Response<String> refundOrder(RefundOrderRequest request);

}
