package cn.bugstack.trigger.http;

import cn.bugstack.api.request.product.CarouselAddRequest;
import cn.bugstack.api.request.product.CarouselUpdateRequest;
import cn.bugstack.api.response.page.PageResponse;
import cn.bugstack.api.response.product.CarouselItemResponse;
import cn.bugstack.api.response.Response;
import cn.bugstack.domain.product.model.entity.ProductCarouselEntity;
import cn.bugstack.domain.product.repository.IProductCarouselRepository;
import cn.bugstack.trigger.assembler.ProductCarouselAssembler;
import cn.bugstack.trigger.interceptor.PublicEndpoint;
import cn.bugstack.trigger.interceptor.RequireRole;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.enums.RoleEnum;
import cn.bugstack.types.enums.CarouselTargetTypeEnum;
import cn.bugstack.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
@Validated
@RequestMapping("/api/v1/product-carousel")
@Slf4j
public class ProductCarouselController {

    @Resource
    private IProductCarouselRepository productCarouselRepository;

    /**
     * 新增轮播项（管理员）
     */
    @RequireRole({RoleEnum.ADMIN})
    @PostMapping("add")
    public Response<Long> add(@Valid @RequestBody CarouselAddRequest request) {
        log.info("新增轮播项开始 targetType:{} title:{}", request.getTargetType(), request.getTitle());
        if (!CarouselTargetTypeEnum.isValid(request.getTargetType())) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "跳转类型非法");
        }
        ProductCarouselEntity entity = ProductCarouselAssembler.toAddEntity(request);
        Long id = productCarouselRepository.save(entity);
        log.info("新增轮播项完成 id:{}", id);
        return Response.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(id)
                .build();
    }

    /**
     * 更新轮播项（管理员）
     */
    @RequireRole({RoleEnum.ADMIN})
    @PutMapping("{id}")
    public Response<Void> update(@PathVariable Long id, @RequestBody CarouselUpdateRequest request) {
        log.info("更新轮播项开始 id:{}", id);
        if (request.getTargetType() != null && !CarouselTargetTypeEnum.isValid(request.getTargetType())) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "跳转类型非法");
        }
        ProductCarouselEntity entity = ProductCarouselAssembler.toUpdateEntity(id, request);
        productCarouselRepository.updateById(entity);
        log.info("更新轮播项完成 id:{}", id);
        return Response.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .build();
    }

    /**
     * 删除轮播项（管理员）
     */
    @RequireRole({RoleEnum.ADMIN})
    @DeleteMapping("{id}")
    public Response<Void> delete(@PathVariable Long id) {
        log.info("删除轮播项开始 id:{}", id);
        productCarouselRepository.deleteById(id);
        log.info("删除轮播项完成 id:{}", id);
        return Response.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .build();
    }

    /**
     * 分页查询轮播项（管理员）
     */
    @RequireRole({RoleEnum.ADMIN})
    @GetMapping("page")
    public Response<PageResponse<CarouselItemResponse>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("分页查询轮播项 pageNo:{} pageSize:{}", pageNo, pageSize);
        List<ProductCarouselEntity> list = productCarouselRepository.queryPage(pageNo, pageSize);
        long total = productCarouselRepository.countPage();
        List<CarouselItemResponse> itemList = list.stream()
                .map(ProductCarouselAssembler::toResponse)
                .collect(Collectors.toList());
        PageResponse<CarouselItemResponse> pageResponse = PageResponse.<CarouselItemResponse>builder()
                .total(total)
                .pageNo(pageNo)
                .pageSize(pageSize)
                .list(itemList)
                .build();
        return Response.<PageResponse<CarouselItemResponse>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(pageResponse)
                .build();
    }

    /**
     * 前台获取启用的轮播列表（公开接口）
     */
    @PublicEndpoint
    @GetMapping("active")
    public Response<List<CarouselItemResponse>> active() {
        log.info("查询启用轮播列表");
        List<ProductCarouselEntity> list = productCarouselRepository.queryActiveList();
        List<CarouselItemResponse> response = list.stream()
                .map(ProductCarouselAssembler::toResponse)
                .collect(Collectors.toList());
        return Response.<List<CarouselItemResponse>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }
}
