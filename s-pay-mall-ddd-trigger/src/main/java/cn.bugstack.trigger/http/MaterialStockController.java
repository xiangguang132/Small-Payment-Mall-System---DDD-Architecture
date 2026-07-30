package cn.bugstack.trigger.http;

import cn.bugstack.api.request.materialstock.MaterialStockAddRequest;
import cn.bugstack.api.request.materialstock.MaterialStockInboundRequest;
import cn.bugstack.api.request.materialstock.MaterialStockQuantityRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.materialstock.MaterialStockDetailResponse;
import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;
import cn.bugstack.domain.materialstock.service.IMaterialStockService;
import cn.bugstack.trigger.assembler.MaterialStockAssembler;
import cn.bugstack.types.enums.ResponseCode;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@CrossOrigin("*")
@Validated
@RequestMapping("/api/v1/material-stock")
public class MaterialStockController {

    @Resource
    private IMaterialStockService materialStockService;

    @GetMapping("{id}")
    public Response<MaterialStockDetailResponse> detail(@PathVariable("id") Long id) {
        return success(MaterialStockAssembler.toDetailResponse(materialStockService.queryStockById(id)));
    }

    @GetMapping("query")
    public Response<MaterialStockDetailResponse> query(@RequestParam("materialId") Long materialId,
                                                       @RequestParam(value = "storageAddress", required = false)
                                                       String storageAddress) {
        MaterialStockAggregate stock =
                materialStockService.queryStockByMaterialIdAndStorageAddress(materialId, storageAddress);
        return success(MaterialStockAssembler.toDetailResponse(stock));
    }

    @PostMapping("add")
    public Response<Long> add(@Valid @RequestBody MaterialStockAddRequest request) {
        return success(materialStockService.addStock(request.getMaterialId(), request.getStorageAddress()));
    }

    @PostMapping("adjust/{id}")
    public Response<Boolean> adjust(@PathVariable("id") Long id,
                                    @Valid @RequestBody MaterialStockQuantityRequest request) {
        materialStockService.adjustStock(id, request.getQuantity(), request.getReason());
        return success(true);
    }

    @PostMapping("inbound")
    public Response<Boolean> inbound(@Valid @RequestBody MaterialStockInboundRequest request) {
        materialStockService.inbound(request.getSupplierMaterialId(), request.getStorageAddress(), request.getQuantity());
        return success(true);
    }

    @PostMapping("outbound/{id}")
    public Response<Boolean> productionOutbound(@PathVariable("id") Long id,
                                                @Valid @RequestBody MaterialStockQuantityRequest request) {
        materialStockService.productionOutbound(id, request.getQuantity());
        return success(true);
    }

    @PostMapping("lock/{id}")
    public Response<Boolean> lock(@PathVariable("id") Long id,
                                  @Valid @RequestBody MaterialStockQuantityRequest request) {
        materialStockService.lock(id, request.getQuantity());
        return success(true);
    }

    @PostMapping("release/{id}")
    public Response<Boolean> release(@PathVariable("id") Long id,
                                     @Valid @RequestBody MaterialStockQuantityRequest request) {
        materialStockService.release(id, request.getQuantity());
        return success(true);
    }

    @PostMapping("confirm-outbound/{id}")
    public Response<Boolean> confirmOutbound(@PathVariable("id") Long id,
                                             @Valid @RequestBody MaterialStockQuantityRequest request) {
        materialStockService.confirmOutbound(id, request.getQuantity());
        return success(true);
    }

    private <T> Response<T> success(T data) {
        return Response.<T>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(data)
                .build();
    }
}
