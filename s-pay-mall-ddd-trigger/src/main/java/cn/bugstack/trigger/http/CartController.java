package cn.bugstack.trigger.http;

import cn.bugstack.api.ICartService;
import cn.bugstack.api.request.cart.AddToCartRequest;
import cn.bugstack.api.request.cart.BatchRemoveCartRequest;
import cn.bugstack.api.request.cart.ToggleCheckRequest;
import cn.bugstack.api.request.cart.UpdateCartQuantityRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.cart.CartDetailResponse;
import cn.bugstack.trigger.assembler.CartAssembler;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/cart/")
public class CartController {

    @Resource
    private ICartService cartService;

    /** 添加到购物车 */
    @RequestMapping(value = "add", method = RequestMethod.POST)
    public Response<Void> addToCart(@RequestBody AddToCartRequest request) {
        String userId = getUserId();
        log.info("添加购物车 userId:{} productId:{}", userId, request.getProductId());
        cartService.addToCart(userId, request.getProductId(),
                request.getQuantity() != null ? request.getQuantity() : 1);
        return Response.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .build();
    }

    /** 查询购物车列表 */
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public Response<List<CartDetailResponse>> listCart() {
        String userId = getUserId();
        List<cn.bugstack.domain.order.model.entity.ShopCartEntity> entities = cartService.listCart(userId);
        return Response.<List<CartDetailResponse>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(CartAssembler.toDetailResponse(entities))
                .build();
    }

    /** 更新数量 */
    @RequestMapping(value = "update_quantity", method = RequestMethod.POST)
    public Response<Void> updateQuantity(@RequestBody UpdateCartQuantityRequest request) {
        String userId = getUserId();
        log.info("更新购物车数量 userId:{} cartId:{} quantity:{}", userId, request.getCartId(), request.getQuantity());
        cartService.updateQuantity(userId, request.getCartId(), request.getQuantity());
        return Response.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .build();
    }

    /** 切换选中状态 */
    @RequestMapping(value = "toggle_check", method = RequestMethod.POST)
    public Response<Void> toggleCheck(@RequestBody ToggleCheckRequest request) {
        String userId = getUserId();
        log.info("切换选中状态 userId:{} cartId:{} checked:{}", userId, request.getCartId(), request.getChecked());
        cartService.toggleCheck(userId, request.getCartId(), request.getChecked());
        return Response.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .build();
    }

    /** 单个删除 */
    @RequestMapping(value = "remove", method = RequestMethod.POST)
    public Response<Void> removeFromCart(@RequestBody java.util.Map<String, Long> body) {
        String userId = getUserId();
        Long cartId = body.get("cartId");
        log.info("删除购物车项 userId:{} cartId:{}", userId, cartId);
        cartService.removeFromCart(userId, cartId);
        return Response.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .build();
    }

    /** 批量删除 */
    @RequestMapping(value = "batch_remove", method = RequestMethod.POST)
    public Response<Void> batchRemove(@RequestBody BatchRemoveCartRequest request) {
        String userId = getUserId();
        log.info("批量删除购物车 userId:{} cartIds:{}", userId, request.getCartIds());
        cartService.batchRemove(userId, request.getCartIds());
        return Response.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .build();
    }

    /** 清空购物车 */
    @RequestMapping(value = "clear", method = RequestMethod.POST)
    public Response<Void> clearCart() {
        String userId = getUserId();
        log.info("清空购物车 userId:{}", userId);
        cartService.clearCart(userId);
        return Response.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .build();
    }

    /** 从登录拦截器中获取 userId */
    private String getUserId() {
        HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String userId = (String) httpRequest.getAttribute("userId");
        if (userId == null || userId.trim().isEmpty()) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "无法识别用户身份，请重新登录");
        }
        return userId;
    }

}
