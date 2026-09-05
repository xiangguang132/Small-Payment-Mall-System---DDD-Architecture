package cn.bugstack.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ICartDao {

    /** 新增 */
    void insert(cn.bugstack.infrastructure.dao.po.cart.Cart cart);

    /** 根据用户+商品查询 */
    cn.bugstack.infrastructure.dao.po.cart.Cart queryByUserAndProduct(
            @Param("userId") String userId,
            @Param("productId") Long productId);

    /** 根据用户ID查询列表 */
    List<cn.bugstack.infrastructure.dao.po.cart.Cart> queryByUserId(@Param("userId") String userId);

    /** 更新数量 */
    void updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    /** 更新选中状态 */
    void updateChecked(@Param("id") Long id, @Param("checked") Integer checked);

    /** 软删除 */
    void deleteById(@Param("id") Long id, @Param("userId") String userId);

    /** 批量软删除 */
    void deleteByIds(@Param("ids") List<Long> ids, @Param("userId") String userId);

    /** 清空用户购物车 */
    void clearByUserId(@Param("userId") String userId);

    /** 根据ID列表+用户ID查询（结算校验用，仅返回 status=1） */
    List<cn.bugstack.infrastructure.dao.po.cart.Cart> queryByIds(
            @Param("ids") List<Long> ids,
            @Param("userId") String userId);

    /** 支付成功后按 userId + productIds 硬删除已结算的购物车记录 */
    void deleteByUserIdAndProductIds(@Param("userId") String userId,
                                     @Param("productIds") List<Long> productIds);

}
