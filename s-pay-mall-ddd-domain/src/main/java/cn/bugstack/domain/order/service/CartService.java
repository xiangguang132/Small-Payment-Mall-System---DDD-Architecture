package cn.bugstack.domain.order.service;

import cn.bugstack.domain.order.adapter.port.IProductPort;
import cn.bugstack.domain.order.model.entity.CartEntity;
import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.model.entity.PayOrderItemEntity;
import cn.bugstack.domain.order.model.entity.ProductEntity;
import cn.bugstack.domain.order.repository.ICartRepository;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CartService implements ICartService {

    @Resource
    private ICartRepository cartRepository;
    @Resource
    private IProductPort productPort;
    @Resource
    private IOrderService orderService;

    @Override
    public void addToCart(String userId, Long productId, Integer quantity) {
        // 已存在则数量+1
        CartEntity existing = cartRepository.queryByUserAndProduct(userId, productId);
        if (existing != null) {
            cartRepository.updateQuantity(existing.getId(), existing.getQuantity() + quantity);
            return;
        }
        // 不存在则新增
        CartEntity entity = CartEntity.builder()
                .userId(userId)
                .productId(productId)
                .quantity(quantity)
                .checked(1)
                .status(1)
                .build();
        cartRepository.save(entity);
    }

    @Override
    public List<CartEntity> listCart(String userId) {
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

    @Override
    public PayOrderEntity checkout(String userId, List<Long> cartIds, List<String> couponIds) {
        // 1. 校验购物车记录归属与状态
        List<CartEntity> carts = cartRepository.queryByIds(cartIds, userId);
        if (carts == null || carts.size() != cartIds.size()) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "购物车项不存在或已删除，请刷新后重试");
        }
        for (CartEntity cart : carts) {
            if (cart.getChecked() == null || cart.getChecked() != 1) {
                throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "存在未勾选的购物车项");
            }
        }

        // 2. 按结算时实时价聚合明细（不信任购物车快照价格）
        List<PayOrderItemEntity> items = new ArrayList<>(carts.size());
        BigDecimal originalAmount = BigDecimal.ZERO;
        for (CartEntity cart : carts) {
            ProductEntity product = productPort.queryProductByProductId(String.valueOf(cart.getProductId()));
            if (product == null) {
                throw new AppException(ResponseCode.NOT_FOUND, "商品不存在，请刷新后重试");
            }
            int quantity = cart.getQuantity() == null || cart.getQuantity() < 1 ? 1 : cart.getQuantity();
            BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            items.add(PayOrderItemEntity.builder()
                    .productId(cart.getProductId())
                    .productName(product.getProductName())
                    .quantity(quantity)
                    .price(product.getPrice())
                    .totalAmount(totalAmount)
                    .build());
            originalAmount = originalAmount.add(totalAmount);
        }

        // 3. 调用下单服务（券在聚合金额上择优抵扣）
        try {
            return orderService.createCartOrder(userId, items, originalAmount, couponIds);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("购物车结算失败 userId:{} cartIds:{}", userId, cartIds, e);
            throw new AppException(ResponseCode.UN_ERROR, "购物车结算失败，请稍后重试");
        }
    }

}