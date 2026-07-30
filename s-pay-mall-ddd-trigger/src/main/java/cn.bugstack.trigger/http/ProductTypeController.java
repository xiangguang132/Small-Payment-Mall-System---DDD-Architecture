package cn.bugstack.trigger.http;

import cn.bugstack.api.request.producttype.ProductTypeAddRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.producttype.ProductTypeDetailResponse;
import cn.bugstack.domain.producttype.model.aggregate.ProductTypeAggregate;
import cn.bugstack.domain.producttype.service.IProductTypeService;
import cn.bugstack.trigger.assembler.ProductTypeAssembler;
import cn.bugstack.types.enums.ResponseCode;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@CrossOrigin("*")
@Validated
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
    public Response<Long> add(@Valid @RequestBody ProductTypeAddRequest request){
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
        ProductTypeAggregate productType = productTypeService.queryProductTypeById(id);
        ProductTypeDetailResponse response = ProductTypeAssembler.toDetailResponse(productType);
        return Response.<ProductTypeDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    /**
     * 依据id修改商品分类状态
     */
    @PutMapping("status/{id}")
    public Response<ProductTypeDetailResponse> onSale(@PathVariable Long id) {
        ProductTypeAggregate updated = productTypeService.onSale(id);
        ProductTypeDetailResponse response = ProductTypeAssembler.toDetailResponse(updated);
        return Response.<ProductTypeDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

}
