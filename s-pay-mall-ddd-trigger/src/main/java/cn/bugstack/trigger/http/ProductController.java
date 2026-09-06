package cn.bugstack.trigger.http;

import cn.bugstack.api.request.product.ProductAddRequest;
import cn.bugstack.api.request.product.ProductPageRequest;
import cn.bugstack.api.request.product.ProductSearchRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.page.PageResponse;
import cn.bugstack.api.response.product.ProductDetailResponse;
import cn.bugstack.api.response.product.ProductListItemResponse;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialRequest;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialResult;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyActivityRepository;
import cn.bugstack.domain.groupbuy.service.trial.IGroupBuyTrialService;
import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import cn.bugstack.domain.product.service.IProductService;
import cn.bugstack.domain.producttype.model.aggregate.ProductTypeAggregate;
import cn.bugstack.domain.producttype.service.IProductTypeService;
import cn.bugstack.trigger.assembler.ProductAssembler;
import cn.bugstack.trigger.assembler.ProductTypeAssembler;
import cn.bugstack.trigger.interceptor.PublicEndpoint;
import cn.bugstack.trigger.interceptor.RequireRole;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.enums.RoleEnum;
import cn.bugstack.types.exception.AppException;
import cn.bugstack.types.common.ImageUrlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
    private IGroupBuyTrialService groupBuyTrialService;
    @Resource
    private IProductTypeService productTypeService;

    /** 图片访问域名前缀，用于将相对路径拼接为完整 URL */
    @Value("${file.base-url:http://localhost:8080}")
    private String fileBaseUrl;

    /**
     * 分页查询商品列表
     * @param request
     * @return
     */
    @PublicEndpoint
    @GetMapping("page")
    public Response<PageResponse<ProductListItemResponse>> page(@Valid ProductPageRequest request) {
        // 兜底：前端未传 pageNo / pageSize 时使用默认值
        if (request.getPageNo() == null) request.setPageNo(1);
        if (request.getPageSize() == null) request.setPageSize(10);
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
        List<ProductListItemResponse> itemList = toItemListWithActivity(products);
        PageResponse<ProductListItemResponse> pageResponse = PageResponse.<ProductListItemResponse>builder()
                .total(total)
                .pageNo(request.getPageNo())
                .pageSize(request.getPageSize())
                .list(itemList)
                .build();
        log.info("分页查询商品列表完成 total:{}", total);
        return Response.<PageResponse<ProductListItemResponse>>builder()
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
    @PublicEndpoint
    @GetMapping("search")
    public Response<PageResponse<ProductListItemResponse>> search(@Valid ProductSearchRequest request) {
        if (request.getPageNo() == null) request.setPageNo(1);
        if (request.getPageSize() == null) request.setPageSize(10);
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
        List<ProductListItemResponse> itemList = toItemListWithActivity(products);
        PageResponse<ProductListItemResponse> pageResponse = PageResponse.<ProductListItemResponse>builder()
                .total(total)
                .pageNo(request.getPageNo())
                .pageSize(request.getPageSize())
                .list(itemList)
                .build();
        log.info("商品全局搜索完成 keyword:{} total:{}", keyword, total);
        return Response.<PageResponse<ProductListItemResponse>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(pageResponse)
                .build();
    }

    private List<ProductListItemResponse> toItemListWithActivity(List<ProductAggregate> products) {
        // 批量查询所有商品的拼团活动，避免 N+1
        List<Long> productIds = products.stream()
                .map(ProductAggregate::getId)
                .collect(Collectors.toList());
        Map<Long, GroupBuyActivityEntity> activityMap = groupBuyActivityRepository
                .queryGroupBuyActivityByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(GroupBuyActivityEntity::getProductId, Function.identity(), (a, b) -> a));

        List<ProductListItemResponse> itemList = new java.util.ArrayList<>();
        for (ProductAggregate product : products) {
            ProductListItemResponse item = new ProductListItemResponse();
            item.setId(product.getId());
            item.setName(product.getName());
            item.setPrice(product.getPrice());
            item.setCoveringImg(ImageUrlUtils.buildFullUrl(fileBaseUrl, product.getCoveringImg()));
            GroupBuyActivityEntity activity = activityMap.get(product.getId());
            if (activity != null) {
                item.setActivityId(activity.getActivityId());
                item.setTargetCount(activity.getTargetCount());
            }
            itemList.add(item);
        }
        return itemList;
    }

    /** 将商品图片相对路径拼接为完整 URL */
    private void fillImageUrl(ProductDetailResponse resp) {
        if (resp == null) {
            return;
        }
        resp.setCoveringImg(ImageUrlUtils.buildFullUrl(fileBaseUrl, resp.getCoveringImg()));
        resp.setImgs(ImageUrlUtils.buildFullUrls(fileBaseUrl, resp.getImgs()));
    }

    /**
     * 添加单个商品
     * @param request
     * @return
     */
    @RequireRole({RoleEnum.ADMIN})
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
    @PublicEndpoint
    @GetMapping("{id}")
    public Response<ProductDetailResponse> detail(@PathVariable Long id) {
        log.info("查询商品详情开始 id:{}", id);
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "商品id不能为空");
        }
        ProductAggregate product = productService.queryProductById(id);
        ProductDetailResponse response = ProductAssembler.toDetailResponse(product);
        fillImageUrl(response);
        // 查询关联的拼团活动，填充 activityId
        GroupBuyActivityEntity activity = groupBuyActivityRepository.queryGroupBuyActivityByProductId(id);
        if (activity != null) {
            response.setActivityId(activity.getActivityId());
            response.setTargetCount(activity.getTargetCount());
            try {
                GroupBuyTrialResult trial = groupBuyTrialService.queryGroupBuyTrial(
                        GroupBuyTrialRequest.builder()
                                .activityId(activity.getActivityId())
                                .productId(id)
                                .build()
                );
                if (trial != null) {
                    response.setTrialPayPrice(trial.getPayPrice());
                    if (trial.getTargetCount() != null) {
                        response.setTargetCount(trial.getTargetCount());
                    }
                }
            } catch (Exception e) {
                log.warn("商品详情拼团试算失败 productId:{} activityId:{} error:{}",
                        id, activity.getActivityId(), e.getMessage());
            }
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
    @RequireRole({RoleEnum.ADMIN})
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
    @RequireRole({RoleEnum.ADMIN})
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
                .coveringImg(request.getCoveringImg() != null ? request.getCoveringImg() : current.getCoveringImg())
                .imgs(request.getImgs() != null ? request.getImgs() : current.getImgs())
                .isDel(current.getIsDel())
                .createTime(current.getCreateTime())
                .updateTime(LocalDateTime.now())
                .build();
        productService.updateProductById(updated);

        ProductAggregate refreshed = productService.queryProductById(id);
        ProductDetailResponse response = ProductAssembler.toDetailResponse(refreshed);
        fillImageUrl(response);
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
    @RequireRole({RoleEnum.ADMIN})
    @PutMapping("status/{id}")
    public Response<ProductDetailResponse> onSale(@PathVariable Long id) {
        log.info("修改商品状态开始 id:{}", id);
        ProductAggregate updated = productService.onSale(id);
        ProductDetailResponse response = ProductAssembler.toDetailResponse(updated);
        fillImageUrl(response);
        log.info("修改商品状态完成 id:{}", id);
        return Response.<ProductDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }


}
