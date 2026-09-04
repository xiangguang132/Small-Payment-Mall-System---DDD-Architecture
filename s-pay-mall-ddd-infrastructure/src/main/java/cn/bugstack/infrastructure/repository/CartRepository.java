package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.order.model.entity.ShopCartEntity;
import cn.bugstack.domain.order.repository.ICartRepository;
import cn.bugstack.infrastructure.dao.ICartDao;
import cn.bugstack.infrastructure.dao.po.cart.Cart;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CartRepository implements ICartRepository {

    @Resource
    private ICartDao cartDao;

    @Override
    public void save(ShopCartEntity entity) {
        Cart cart = Cart.builder()
                .userId(entity.getUserId())
                .productId(entity.getProductId())
                .quantity(entity.getQuantity())
                .checked(entity.getChecked())
                .status(entity.getStatus())
                .build();
        cartDao.insert(cart);
    }

    @Override
    public ShopCartEntity queryByUserAndProduct(String userId, Long productId) {
        Cart cart = cartDao.queryByUserAndProduct(userId, productId);
        return toEntity(cart);
    }

    @Override
    public List<ShopCartEntity> queryByUserId(String userId) {
        return cartDao.queryByUserId(userId).stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void updateQuantity(Long id, Integer quantity) {
        cartDao.updateQuantity(id, quantity);
    }

    @Override
    public void updateChecked(Long id, Integer checked) {
        cartDao.updateChecked(id, checked);
    }

    @Override
    public void deleteById(Long id, String userId) {
        cartDao.deleteById(id, userId);
    }

    @Override
    public void deleteByIds(List<Long> ids, String userId) {
        cartDao.deleteByIds(ids, userId);
    }

    @Override
    public void clearByUserId(String userId) {
        cartDao.clearByUserId(userId);
    }

    private ShopCartEntity toEntity(Cart cart) {
        if (cart == null) return null;
        return ShopCartEntity.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .productId(cart.getProductId())
                .quantity(cart.getQuantity())
                .checked(cart.getChecked())
                .status(cart.getStatus())
                .createTime(cart.getCreateTime())
                .updateTime(cart.getUpdateTime())
                .build();
    }

}
