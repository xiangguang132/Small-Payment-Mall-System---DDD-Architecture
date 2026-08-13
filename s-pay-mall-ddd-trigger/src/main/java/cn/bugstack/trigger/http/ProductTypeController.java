package cn.bugstack.trigger.http;

import cn.bugstack.api.request.producttype.ProductTypeAddRequest;
import cn.bugstack.api.request.producttype.ProductTypeUpdateRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.producttype.ProductTypeDetailResponse;
import cn.bugstack.domain.producttype.model.aggregate.ProductTypeAggregate;
import cn.bugstack.domain.producttype.service.IProductTypeService;
import cn.bugstack.trigger.assembler.ProductTypeAssembler;
import cn.bugstack.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@CrossOrigin("*")
@Validated
@RequestMapping("/api/v1/product-type")
@Slf4j
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
        log.info("新增商品分类开始 request:{}", request);
        // 把外部传入的数据，转换成领域模型里的“商品类型聚合根对象”
        ProductTypeAggregate productType = ProductTypeAssembler.toAggregate(request);
        // 把 productType 这个商品类型对象交给服务层去新增，新增成功后返回一个 ID，保存到 id 里
        Long id = productTypeService.addNewProductType(productType);
        log.info("新增商品分类完成 id:{}", id);
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
        log.info("删除商品分类开始 id:{}", id);
        productTypeService.deleteProductTypeById(id);
        log.info("删除商品分类完成 id:{}", id);
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(true)
                .build();
    }

    @PutMapping("{id}")
    public Response<ProductTypeDetailResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody ProductTypeUpdateRequest request) {
        log.info("更新商品分类开始 id:{} request:{}", id, request);
        ProductTypeAggregate current = productTypeService.queryProductTypeById(id);
        ProductTypeAggregate updated = ProductTypeAssembler.toUpdatedAggregate(current, request);
        productTypeService.updateProductTypeById(updated);

        ProductTypeAggregate refreshed = productTypeService.queryProductTypeById(id);
        ProductTypeDetailResponse response = ProductTypeAssembler.toDetailResponse(refreshed);
        log.info("更新商品分类完成 id:{}", id);
        return Response.<ProductTypeDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    /**
     * 依据id获取添加商品分类详情
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public Response<ProductTypeDetailResponse> detail(@PathVariable Long id){
        log.info("查询商品分类详情开始 id:{}", id);
        ProductTypeAggregate productType = productTypeService.queryProductTypeById(id);
        ProductTypeDetailResponse response = ProductTypeAssembler.toDetailResponse(productType);
        log.info("查询商品分类详情完成 id:{}", id);
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
        log.info("修改商品分类状态开始 id:{}", id);
        ProductTypeAggregate updated = productTypeService.onSale(id);
        ProductTypeDetailResponse response = ProductTypeAssembler.toDetailResponse(updated);
        log.info("修改商品分类状态完成 id:{}", id);
        return Response.<ProductTypeDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

}
