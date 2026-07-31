package cn.bugstack.trigger.http;

import cn.bugstack.api.request.product.ProductAddRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.product.ProductDetailResponse;
import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import cn.bugstack.domain.product.service.IProductService;
import cn.bugstack.trigger.assembler.ProductAssembler;
import cn.bugstack.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;

@RestController
@CrossOrigin("*")
@Validated
@RequestMapping("/api/v1/product")
@Slf4j
public class ProductController {

    @Resource
    private IProductService productService;

    /**
     * 添加单个商品
     * @param request
     * @return
     */
    @PostMapping("add")
    public Response<Long> add(@Valid @RequestBody ProductAddRequest request) {
        log.info("新增商品开始 request:{}", request);
        ProductAggregate product = ProductAssembler.toAggregate(request);
        Long id = productService.addNewProduct(product);
        log.info("新增商品完成 id:{}", id);
        return Response.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(id)
                .build();
    }

    /**
     * 依据id获取商品详情
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public Response<ProductDetailResponse> detail(@PathVariable Long id) {
        log.info("查询商品详情开始 id:{}", id);
        if (id == null) {
            throw new IllegalArgumentException("商品id不能为空");
        }
        ProductAggregate product = productService.queryProductById(id);
        ProductDetailResponse response = ProductAssembler.toDetailResponse(product);
        log.info("查询商品详情完成 id:{}", id);
        return Response.<ProductDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    /**
     * 依据id删除单个商品
     */
    @DeleteMapping("{id}")
    public Response<Boolean> delete(@PathVariable Long id) {
        log.info("删除商品开始 id:{}", id);
        if (id == null) {
            throw new IllegalArgumentException("商品id不能为空");
        }
        productService.deleteProductById(id);
        log.info("删除商品完成 id:{}", id);
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(true)
                .build();
    }

    /**
     * 依据id更新单个商品
     */
    @PutMapping("{id}")
    public Response<ProductDetailResponse> update(@PathVariable Long id, @RequestBody ProductAddRequest request) {
        log.info("更新商品开始 id:{} request:{}", id, request);
        ProductAggregate current = productService.queryProductById(id);
        if (current == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        ProductAggregate updated = ProductAggregate.builder()
                .id(current.getId())
                .name(request.getName() != null ? request.getName().trim() : current.getName())
                .description(request.getDescription() != null ? request.getDescription().trim() : current.getDescription())
                .sku(request.getSku() != null ? request.getSku().trim() : current.getSku())
                .categoryId(request.getCategoryId() != null ? request.getCategoryId() : current.getCategoryId())
                .status(request.getStatus() != null ? request.getStatus() : current.getStatus())
                .price(request.getPrice() != null ? request.getPrice() : current.getPrice())
                .isDel(current.getIsDel())
                .createTime(current.getCreateTime())
                .updateTime(LocalDateTime.now())
                .build();
        productService.updateProductById(updated);

        ProductAggregate refreshed = productService.queryProductById(id);
        ProductDetailResponse response = ProductAssembler.toDetailResponse(refreshed);
        log.info("更新商品完成 id:{}", id);
        return Response.<ProductDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    /**
     * 商品上下架
     */
    @PutMapping("status/{id}")
    public Response<ProductDetailResponse> onSale(@PathVariable Long id) {
        log.info("修改商品状态开始 id:{}", id);
        ProductAggregate updated = productService.onSale(id);
        ProductDetailResponse response = ProductAssembler.toDetailResponse(updated);
        log.info("修改商品状态完成 id:{}", id);
        return Response.<ProductDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }
}
