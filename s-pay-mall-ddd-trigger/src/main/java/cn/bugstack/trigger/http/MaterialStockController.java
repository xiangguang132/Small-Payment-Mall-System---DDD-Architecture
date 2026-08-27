package cn.bugstack.trigger.http;

import cn.bugstack.api.request.materialstock.MaterialStockInboundRequest;
import cn.bugstack.api.request.materialstock.MaterialStockQuantityRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.materialstock.MaterialStockDetailResponse;
import cn.bugstack.api.response.materialstock.MaterialStockManualOutboundResponse;
import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;
import cn.bugstack.domain.materialstock.service.IMaterialStockService;
import cn.bugstack.trigger.assembler.MaterialStockAssembler;
import cn.bugstack.trigger.interceptor.RequireRole;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.enums.RoleEnum;
import cn.bugstack.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController
@CrossOrigin("*")
@Validated
@RequestMapping("/api/v1/material-stock")
@RequireRole({RoleEnum.ADMIN, RoleEnum.INVENTORY_MANAGER})
@Slf4j
public class MaterialStockController {

    @Resource
    private IMaterialStockService materialStockService;

    @GetMapping("{id}")
    public Response<MaterialStockDetailResponse> detail(@PathVariable @NotNull Long id) {
        log.info("查询原料库存详情开始 id:{}", id);
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料库存的id不能为空");
        }
        MaterialStockAggregate materialStockAggregate = materialStockService.queryMaterialStockById(id);
        MaterialStockDetailResponse response = MaterialStockAssembler.toDetailResponse(materialStockAggregate);
        log.info("查询原料库存详情完成 id:{}", id);
        return Response.<MaterialStockDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("原料库-查单条成功")
                .data(response)
                .build();
    }

    /**
     * || 统一的 || 原料入库
     * @param request
     * @return
     */
    @PostMapping("inbound")
    public Response<Boolean> inbound(@Valid @RequestBody MaterialStockInboundRequest request) {
        log.info("原料入库开始 request:{}", request);
        materialStockService.inbound(
                request.getMaterialId(),
                request.getStorageAddress(),
                request.getInboundQty(),
                request.getReason()
        );
        log.info("原料入库完成 materialId:{} storageAddress:{}", request.getMaterialId(), request.getStorageAddress());

        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("原料库-统一入库成功")
                .data(true)
                .build();
    }

    /**
     * 人工类型出库
     * 不需要 锁库
     * @param id
     * @param request
     * @return
     */
    @PostMapping("manual-outbound/{id}")
    public Response<MaterialStockManualOutboundResponse> outbound(@PathVariable("id") @NotNull Long id,
                                                          @Valid @RequestBody MaterialStockQuantityRequest request) {
        log.info("人工原料出库开始 id:{} request:{}", id, request);
        MaterialStockAggregate updated = materialStockService.manualOutbound(id, request.getQuantity(), request.getReason());
        MaterialStockManualOutboundResponse response = MaterialStockAssembler.toManualOutboundResponse(updated);
        log.info("人工原料出库完成 id:{} availableQty:{} totalQty:{}", id, response.getAvailableQty(), response.getTotalQty());
        return Response.<MaterialStockManualOutboundResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("原料库-人工出库成功")
                .data(response)
                .build();
    }

    /**
     * 人工类型出库
     * 不需要 锁库
     * @param id
     * @param request
     * @return
     */
    @PostMapping("adjust/{id}")
    public Response<MaterialStockDetailResponse>  adjust(@PathVariable("id") @NotNull Long id,
                                                         @Valid @RequestBody MaterialStockInboundRequest request) {
        log.info("人工原料调库开始 id:{} request:{}", id, request);
        MaterialStockAggregate updated = materialStockService.adjust(
                id,
                request.getMaterialId(),
                request.getStorageAddress(),
                request.getInboundQty(),
                request.getReason()
        );
        MaterialStockDetailResponse response = MaterialStockAssembler.toDetailResponse(updated);
        log.info("人工原料调库完成 id:{} availableQty:{} totalQty:{}", id, response.getAvailableQty(), response.getTotalQty());
        return Response.<MaterialStockDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("原料库-人工调库成功")
                .data(response)
                .build();
    }

    /**
     * 依据 id 锁库
     * @param id
     * @param request
     * @return
     */
    @PostMapping("lock/{id}")
    public Response<Boolean> lock(@PathVariable("id") Long id,
                                  @Valid @RequestBody MaterialStockQuantityRequest request
                                  ) {
        log.info("锁定原料库存开始 id:{} request:{}", id, request);
        materialStockService.lock(id, request.getQuantity());
        log.info("锁定原料库存完成 id:{}", id);
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("原料库-锁库成功")
                .data(true)
                .build();
    }

    /**
     * 流水线-放库
     * 需要 锁库
     * @param id
     * @param request
     * @return
     */
    @PostMapping("release/{id}")
    public Response<Boolean> release(@PathVariable("id") Long id,
                                     @Valid @RequestBody MaterialStockQuantityRequest request) {
        materialStockService.release(id, request.getQuantity());
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("原料库-放库成功")
                .data(true)
                .build();
    }

    /**
     * 流水线-出库
     * 需要 锁库
     * @param id
     * @param request
     * @return
     */
    @PostMapping("auto-outbound/{id}")
    public Response<MaterialStockManualOutboundResponse> autoOutBound(@PathVariable("id") @NotNull Long id,
                                                                      @Valid @RequestBody MaterialStockQuantityRequest request) {
        log.info("流水线原料出库开始 id:{} request:{}", id, request);
        MaterialStockAggregate updated = materialStockService.autoOutbound(id, request.getQuantity(), request.getReason());
        MaterialStockManualOutboundResponse response = MaterialStockAssembler.toManualOutboundResponse(updated);
        log.info("流水线原料出库完成 id:{} lockedQty:{} totalQty:{}", id, response.getLockedQty(), response.getTotalQty());
        return Response.<MaterialStockManualOutboundResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("原料库-流水线出库成功")
                .data(response)
                .build();
    }
}
