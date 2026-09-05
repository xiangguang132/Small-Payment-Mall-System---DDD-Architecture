package cn.bugstack.domain.order.service;

import cn.bugstack.domain.order.model.entity.CartEntity;
import cn.bugstack.domain.order.model.entity.PayOrderEntity;

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

    /**
     * 购物车结算：校验勾选记录 → 按实时价聚合 → 聚合成一笔 CART 订单 → 拉起支付宝
     * @param userId 用户ID
     * @param cartIds 勾选的购物车项ID列表
     * @param couponIds 用户选择的优惠券ID列表（可空）
     * @return 支付单（含 outTradeNo + payUrl）
     */
    PayOrderEntity checkout(String userId, List<Long> cartIds, List<String> couponIds);

}
