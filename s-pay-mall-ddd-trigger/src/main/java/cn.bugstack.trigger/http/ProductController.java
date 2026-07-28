package cn.bugstack.trigger.http;

import cn.bugstack.api.request.product.ProductAddRequest;
import cn.bugstack.api.request.product.ProductUpdateRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.product.ProductDetailResponse;
import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import cn.bugstack.domain.product.service.IProductService;
import cn.bugstack.trigger.assembler.ProductAssembler;
import cn.bugstack.types.enums.ResponseCode;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/product")
public class ProductController {

    @Resource
    private IProductService productService;

    /**
     * 添加单个商品
     * @param request
     * @return
     */
    @PostMapping("add")
    public Response<Long> add(@RequestBody ProductAddRequest request) {
        ProductAggregate product = ProductAssembler.toAggregate(request);
        Long id = productService.addNewProduct(product);
        return Response.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(id)
                .build();
    }

    /**
     * 依据id修改商品详情
     * @param request
     * @return
     */
    @PostMapping("update")
    public Response<ProductDetailResponse> update(@RequestBody ProductUpdateRequest request) {
        ProductAggregate product = ProductAssembler.toAggregate(request);
        ProductAggregate updated = productService.updateProduct(product);
        ProductDetailResponse response = ProductAssembler.toDetailResponse(updated);
        return Response.<ProductDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    /**
     * 依据id获取商品详情
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public Response<ProductDetailResponse> detail(@PathVariable Long id) {
        if (id == null) {
            throw new IllegalArgumentException("商品id不能为空");
        }
        ProductAggregate product = productService.queryProductById(id);
        ProductDetailResponse response = ProductAssembler.toDetailResponse(product);
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
        if (id == null) {
            throw new IllegalArgumentException("商品id不能为空");
        }
        productService.deleteProductById(id);
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(true)
                .build();
    }
}
