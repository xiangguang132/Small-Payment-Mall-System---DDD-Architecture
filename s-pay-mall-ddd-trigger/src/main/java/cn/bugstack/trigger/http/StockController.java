package cn.bugstack.trigger.http;

import cn.bugstack.api.response.Response;
import cn.bugstack.api.request.warehousestock.StockAdjustRequest;
import cn.bugstack.api.response.warehousestock.StockDetailResponse;
import cn.bugstack.domain.warehousestock.model.aggregate.StockAggregate;
import cn.bugstack.domain.warehousestock.service.IStockService;
import cn.bugstack.trigger.assembler.StockAssembler;
import cn.bugstack.trigger.interceptor.RequireRole;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.enums.RoleEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@CrossOrigin("*")
@Validated
@RequestMapping("/api/v1/warehouse-stock")
@RequireRole({RoleEnum.ADMIN, RoleEnum.INVENTORY_MANAGER})
@Slf4j
public class StockController {

    @Resource
    private IStockService stockService;

    /**
     * 依据id获取单条库存详情
     */
    @GetMapping("{id}")
    public Response<StockDetailResponse> detail(@PathVariable("id") Long id){
        log.info("查询仓库库存详情开始 id:{}", id);
        StockAggregate stock = stockService.queryStockById(id);
        StockDetailResponse stockDetailResponse = StockAssembler.toDetailResponse(stock);
        log.info("查询仓库库存详情完成 id:{}", id);
        return Response.<StockDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(stockDetailResponse)
                .build();
    }

    /**
     * 依据仓库和商品获取库存详情
     */
    @GetMapping("query")
    public Response<StockDetailResponse> queryByWarehouseAndProduct(@RequestParam("warehouseId") Long warehouseId,
                                                                    @RequestParam("productId") Long productId) {
        log.info("按仓库商品查询库存开始 warehouseId:{} productId:{}", warehouseId, productId);
        StockAggregate stock = stockService.queryStockByWarehouseIdAndProductId(warehouseId, productId);
        StockDetailResponse stockDetailResponse = StockAssembler.toDetailResponse(stock);
        log.info("按仓库商品查询库存完成 warehouseId:{} productId:{}", warehouseId, productId);
        return Response.<StockDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(stockDetailResponse)
                .build();
    }

    /**
     * 新增一条库存记录
     */
    @PostMapping("add")
    public Response<Long> add(@Valid @RequestBody StockAdjustRequest request) {
        log.info("新增仓库库存开始 request:{}", request);
        Long id = stockService.addStock(request.getWarehouseId(), request.getProductId());
        log.info("新增仓库库存完成 id:{}", id);
        return Response.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(id)
                .build();
    }

    /**
     * 调整库存，可正可负
     */
    @PostMapping("adjust")
    public Response<Boolean> adjust(@Valid @RequestBody StockAdjustRequest request) {
        log.info("调整仓库库存开始 request:{}", request);
        stockService.adjustStock(request.getWarehouseId(), request.getProductId(), request.getQuantity(), request.getReason());
        log.info("调整仓库库存完成 warehouseId:{} productId:{}", request.getWarehouseId(), request.getProductId());
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(true)
                .build();
    }

    /**
     * 入库加库存
     */
    @PostMapping("inbound")
    public Response<Boolean> inbound(@Valid @RequestBody StockAdjustRequest request) {
        log.info("仓库库存入库开始 request:{}", request);
        stockService.inbound(request.getWarehouseId(), request.getProductId(), request.getQuantity());
        log.info("仓库库存入库完成 warehouseId:{} productId:{}", request.getWarehouseId(), request.getProductId());
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(true)
                .build();
    }

    /**
     * 出库减库存
     */
    @PostMapping("outbound")
    public Response<Boolean> outbound(@Valid @RequestBody StockAdjustRequest request) {
        log.info("仓库库存出库开始 request:{}", request);
        stockService.outbound(request.getWarehouseId(), request.getProductId(), request.getQuantity());
        log.info("仓库库存出库完成 warehouseId:{} productId:{}", request.getWarehouseId(), request.getProductId());
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(true)
                .build();
    }
}
