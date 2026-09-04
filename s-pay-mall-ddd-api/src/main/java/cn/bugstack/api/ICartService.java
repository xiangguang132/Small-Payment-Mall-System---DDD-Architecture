package cn.bugstack.api;

import cn.bugstack.api.request.cart.AddToCartRequest;
import cn.bugstack.api.request.cart.BatchRemoveCartRequest;
import cn.bugstack.api.request.cart.ToggleCheckRequest;
import cn.bugstack.api.request.cart.UpdateCartQuantityRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.cart.CartDetailResponse;

import java.util.List;

public interface ICartService {

    /** 添加到购物车 */
    Response<Void> addToCart(String userId, AddToCartRequest request);

    /** 查询购物车列表 */
    Response<List<CartDetailResponse>> listCart(String userId);

    /** 更新数量 */
    Response<Void> updateQuantity(String userId, UpdateCartQuantityRequest request);

    /** 切换选中状态 */
    Response<Void> toggleCheck(String userId, ToggleCheckRequest request);

    /** 单个删除 */
    Response<Void> removeFromCart(String userId, Long cartId);

    /** 批量删除 */
    Response<Void> batchRemove(String userId, BatchRemoveCartRequest request);

    /** 清空购物车 */
    Response<Void> clearCart(String userId);

}
