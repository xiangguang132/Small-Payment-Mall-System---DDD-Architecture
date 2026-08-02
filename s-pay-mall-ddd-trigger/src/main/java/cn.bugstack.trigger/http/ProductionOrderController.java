package cn.bugstack.trigger.http;

import cn.bugstack.api.request.production.ProductionOrderCreateRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.production.ProductionOrderDetailResponse;
import cn.bugstack.domain.production.model.aggregate.ProductionOrderAggregate;
import cn.bugstack.domain.production.model.vo.ProductionOrderMaterialVO;
import cn.bugstack.domain.production.service.IProductionOrderService;
import cn.bugstack.trigger.assembler.ProductionOrderAssembler;
import cn.bugstack.types.enums.ResponseCode;
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
@RequestMapping("/api/v1/production-order")
@Slf4j
public class ProductionOrderController {

    @Resource
    private IProductionOrderService productionOrderService;

    @GetMapping("{id}")
    public Response<ProductionOrderDetailResponse> detail(@PathVariable Long id) {
        log.info("查询生产需求单详情开始 id：{}", id);
        if (id == null) {
            throw new IllegalArgumentException("商品需求单不存在");
        }

        ProductionOrderAggregate productionOrder = productionOrderService.queryProductionOrderById(id);
        ProductionOrderDetailResponse response = ProductionOrderAssembler.toDetailResponse(productionOrder);
        log.info("查询商品生产需求单详情完成 id：{}", id);
        return Response.<ProductionOrderDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("查询商品生产需求单详情完成")
                .data(response)
                .build();
    }

    @PostMapping("create")
    public Response<Long> create(@Valid @RequestBody ProductionOrderCreateRequest request) {
        log.info("创建生产需求单开始 request:{}", request);

        // 将请求中的原始材料数据，批量转换并封装成数据库实体对象（Entity），最后收集成一个列表
        List<ProductionOrderMaterialVO> materials = request.getMaterials().stream()
                .map(item -> ProductionOrderMaterialVO.builder()
                        .materialId(item.getMaterialId())
                        .materialQuantity(item.getQuantity())
                        .status(0)
                        .isDel(0)
                        .build())
                .collect(Collectors.toList());

        Long orderId = productionOrderService.createOrder(
                request.getProductId(),
                request.getProductQuantity(),
                request.getWarehouseId(),
                materials
        );

        log.info("创建生产需求单完成 orderId:{}", orderId);
        return Response.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(orderId)
                .build();
    }

}
