package cn.bugstack.domain.order.service;

import cn.bugstack.domain.order.model.entity.ShopCartEntity;
import cn.bugstack.domain.order.repository.ICartRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class CartService implements ICartService {

    @Resource
    private ICartRepository cartRepository;

    @Override
    public void addToCart(String userId, Long productId, Integer quantity) {
        // 已存在则数量+1
        ShopCartEntity existing = cartRepository.queryByUserAndProduct(userId, productId);
        if (existing != null) {
            cartRepository.updateQuantity(existing.getId(), existing.getQuantity() + quantity);
            return;
        }
        // 不存在则新增
        ShopCartEntity entity = ShopCartEntity.builder()
                .userId(userId)
                .productId(productId)
                .quantity(quantity)
                .checked(1)
                .status(1)
                .build();
        cartRepository.save(entity);
    }

    @Override
    public List<ShopCartEntity> listCart(String userId) {
        return cartRepository.queryByUserId(userId);
    }

    @Override
    public void updateQuantity(String userId, Long cartId, Integer quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("数量不能小于1");
        }
        cartRepository.updateQuantity(cartId, quantity);
    }

    @Override
    public void toggleCheck(String userId, Long cartId, Integer checked) {
        cartRepository.updateChecked(cartId, checked);
    }

    @Override
    public void removeFromCart(String userId, Long cartId) {
        cartRepository.deleteById(cartId, userId);
    }

    @Override
    public void batchRemove(String userId, List<Long> cartIds) {
        if (cartIds == null || cartIds.isEmpty()) {
            return;
        }
        cartRepository.deleteByIds(cartIds, userId);
    }

    @Override
    public void clearCart(String userId) {
        cartRepository.clearByUserId(userId);
    }

}
