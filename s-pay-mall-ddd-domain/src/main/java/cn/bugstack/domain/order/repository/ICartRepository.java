package cn.bugstack.domain.order.repository;

import cn.bugstack.domain.order.model.entity.ShopCartEntity;

import java.util.List;

public interface ICartRepository {

    /** 新增购物车项 */
    void save(ShopCartEntity entity);

    /** 根据用户+商品查询（用于判断是否已存在） */
    ShopCartEntity queryByUserAndProduct(String userId, Long productId);

    /** 查询用户购物车列表 */
    List<ShopCartEntity> queryByUserId(String userId);

    /** 更新数量 */
    void updateQuantity(Long id, Integer quantity);

    /** 更新选中状态 */
    void updateChecked(Long id, Integer checked);

    /** 软删除（单个） */
    void deleteById(Long id, String userId);

    /** 批量软删除 */
    void deleteByIds(List<Long> ids, String userId);

    /** 清空用户购物车 */
    void clearByUserId(String userId);

}
