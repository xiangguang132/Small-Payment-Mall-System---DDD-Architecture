package cn.bugstack.domain.order.service;

import cn.bugstack.domain.order.model.entity.CartEntity;

import java.util.List;

public interface ICartService {

    /** 添加到购物车（已存在则数量+1） */
    void addToCart(String userId, Long productId, Integer quantity);

    /** 查询购物车列表 */
    List<CartEntity> listCart(String userId);

    /** 更新数量 */
    void updateQuantity(String userId, Long cartId, Integer quantity);

    /** 切换选中状态 */
    void toggleCheck(String userId, Long cartId, Integer checked);

    /** 单个删除 */
    void removeFromCart(String userId, Long cartId);

    /** 批量删除 */
    void batchRemove(String userId, List<Long> cartIds);

    /** 清空购物车 */
    void clearCart(String userId);

}
