package cn.bugstack.trigger.http;

import cn.bugstack.api.request.producttype.ProductTypeAddRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.product.ProductDetailResponse;
import cn.bugstack.api.response.producttype.ProductTypeDetailResponse;
import cn.bugstack.domain.producttype.model.aggregate.ProductTypeAggregate;
import cn.bugstack.domain.producttype.service.IProductTypeService;
import cn.bugstack.trigger.assembler.ProductAssembler;
import cn.bugstack.trigger.assembler.ProductTypeAssembler;
import cn.bugstack.types.enums.ResponseCode;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/product-type")
public class ProductTypeController {

    @Resource
    private IProductTypeService productTypeService;

    /**
     * 添加商品分类
     * @param request
     * @return
     */
    @PostMapping("add")
    public Response<Long> add(@RequestBody ProductTypeAddRequest request){
        ProductTypeAggregate productType = ProductTypeAssembler.toAggregate(request);
        Long id = productTypeService.addNewProductType(productType);
        return Response.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(id)
                .build();
    }

    /**
     * 依据id删除商品分类
     * @param id
     * @return
     */
    @DeleteMapping("{id}")
    public Response<Boolean> delete(@PathVariable Long id){
        if (id == null) {
            throw new IllegalArgumentException("商品分类id不能为空");
        }
        productTypeService.deleteProductTypeById(id);
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(true)
                .build();
    }

    /**
     * 依据id获取添加商品分类详情
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public Response<ProductTypeDetailResponse> detail(@PathVariable Long id){
        if (id == null) {
            throw new IllegalArgumentException("商品分类id不能为空");
        }
        ProductTypeAggregate productType = productTypeService.queryProductTypeById(id);
        ProductTypeDetailResponse response = ProductTypeAssembler.toDetailResponse(productType);
        return Response.<ProductTypeDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

}
