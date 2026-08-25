package cn.bugstack.trigger.http;

import cn.bugstack.api.request.product.ProductAddRequest;
import cn.bugstack.api.request.product.ProductPageRequest;
import cn.bugstack.api.request.product.ProductSearchRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.page.PageResponse;
import cn.bugstack.api.response.product.ProductDetailResponse;
import cn.bugstack.api.response.producttype.ProductTypeDetailResponse;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyActivityRepository;
import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import cn.bugstack.domain.product.service.IProductService;
import cn.bugstack.domain.producttype.model.aggregate.ProductTypeAggregate;
import cn.bugstack.domain.producttype.service.IProductTypeService;
import cn.bugstack.trigger.assembler.ProductAssembler;
import cn.bugstack.trigger.assembler.ProductTypeAssembler;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
@Validated
@RequestMapping("/api/v1/product")
@Slf4j
public class ProductController {

    @Resource
    private IProductService productService;
    @Resource
    private IGroupBuyActivityRepository groupBuyActivityRepository;
    @Resource
    private IProductTypeService productTypeService;

    /**
     * 分页查询商品列表
     * @param request
     * @return
     */
    @GetMapping("page")
    public Response<PageResponse<ProductDetailResponse>> page(@Valid ProductPageRequest request) {
        log.info("分页查询商品列表开始 request:{}", request);
        List<ProductAggregate> products = productService.queryProductPage(
                request.getName(),
                request.getSku(),
                request.getCategoryId(),
                request.getStatus(),
                request.getPageNo(),
                request.getPageSize()
        );
        Long total = productService.countProductPage(
                request.getName(),
                request.getSku(),
                request.getCategoryId(),
                request.getStatus()
        );
        List<ProductDetailResponse> detailList = toDetailListWithActivity(products);
        PageResponse<ProductDetailResponse> pageResponse = PageResponse.<ProductDetailResponse>builder()
                .total(total)
                .pageNo(request.getPageNo())
                .pageSize(request.getPageSize())
                .list(detailList)
                .build();
        log.info("分页查询商品列表完成 total:{}", total);
        return Response.<PageResponse<ProductDetailResponse>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(pageResponse)
                .build();
    }

    /**
     * 商品全局搜索（依据关键词模糊查询，分页返回上架商品）
     * @param request
     * @return
     */
    @GetMapping("search")
    public Response<PageResponse<ProductDetailResponse>> search(@Valid ProductSearchRequest request) {
        String keyword = request.getKeyword() == null ? null : request.getKeyword().trim();
        if (keyword == null || keyword.isEmpty()) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "搜索关键词不能为空");
        }
        log.info("商品全局搜索开始 keyword:{} pageNo:{} pageSize:{}", keyword, request.getPageNo(), request.getPageSize());
        List<ProductAggregate> products = productService.queryProductSearch(
                keyword,
                request.getPageNo(),
                request.getPageSize()
        );
        Long total = productService.countProductSearch(keyword);
        List<ProductDetailResponse> detailList = toDetailListWithActivity(products);
        PageResponse<ProductDetailResponse> pageResponse = PageResponse.<ProductDetailResponse>builder()
                .total(total)
                .pageNo(request.getPageNo())
                .pageSize(request.getPageSize())
                .list(detailList)
                .build();
        log.info("商品全局搜索完成 keyword:{} total:{}", keyword, total);
        return Response.<PageResponse<ProductDetailResponse>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(pageResponse)
                .build();
    }

    private List<ProductDetailResponse> toDetailListWithActivity(List<ProductAggregate> products) {
        // 为每个商品查询关联的拼团活动，填充 activityId
        List<ProductDetailResponse> detailList = new java.util.ArrayList<>();
        for (ProductAggregate product : products) {
            ProductDetailResponse resp = ProductAssembler.toDetailResponse(product);
            GroupBuyActivityEntity activity = groupBuyActivityRepository.queryGroupBuyActivityByProductId(product.getId());
            if (activity != null) {
                resp.setActivityId(activity.getActivityId());
            }
            detailList.add(resp);
        }
        return detailList;
    }

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
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品id不能为空");
        }
        ProductAggregate product = productService.queryProductById(id);
        ProductDetailResponse response = ProductAssembler.toDetailResponse(product);
        // 查询关联的拼团活动，填充 activityId
        GroupBuyActivityEntity activity = groupBuyActivityRepository.queryGroupBuyActivityByProductId(id);
        if (activity != null) {
            response.setActivityId(activity.getActivityId());
        }
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
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品id不能为空");
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
            throw new AppException(ResponseCode.NOT_FOUND, "商品不存在");
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
