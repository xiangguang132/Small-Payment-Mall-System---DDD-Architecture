package cn.bugstack.trigger.http;

import cn.bugstack.api.request.materialstockallocation.MaterialStockAllocationCreateRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.materialstockallocation.MaterialStockAllocationDetailResponse;
import cn.bugstack.domain.materialstockallocation.model.aggregate.MaterialStockAllocationAggregate;
import cn.bugstack.domain.materialstockallocation.service.IMaterialStockAllocationService;
import cn.bugstack.trigger.assembler.MaterialStockAllocationAssembler;
import cn.bugstack.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@RestController
@CrossOrigin("*")
@Validated
@RequestMapping("/api/v1/material-stock-allocation")
@Slf4j
public class MaterialStockAllocationController {

    @Resource
    private IMaterialStockAllocationService materialStockAllocationService;

    /**
     * 创建一张跨库位分配单。
     */
    @PostMapping("create")
    public Response<String> create(@Valid @RequestBody MaterialStockAllocationCreateRequest request) {
        log.info("创建原料库存分配单开始 request:{}", request);
        String allocationNo = materialStockAllocationService.create(
                request.getRequestStockId(),
                request.getQuantity(),
                request.getReason()
        );

        log.info("创建原料库存分配单完成 allocationNo:{} requestStockId:{} quantity:{}", allocationNo, request.getRequestStockId(), request.getQuantity());
        return Response.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("原料库存分配单-创建成功")
                .data(allocationNo)
                .build();
    }

    /**
     * 按分配单号查询分配详情，返回主单和明细。
     */
    @GetMapping("{allocationNo}")
    public Response<MaterialStockAllocationDetailResponse> detail(@PathVariable("allocationNo") @NotBlank String allocationNo) {
        log.info("查询原料库存分配单详情开始 allocationNo:{}", allocationNo);
        if (allocationNo == null) {
            throw new IllegalArgumentException("分配单号不能为空");
        }
        MaterialStockAllocationAggregate materialStockAllocationAggregate = materialStockAllocationService.queryByAllocationNo(allocationNo);
        MaterialStockAllocationDetailResponse response = MaterialStockAllocationAssembler.toDetailResponse(materialStockAllocationAggregate);
        log.info("查询原料库存分配单详情完成 allocationNo：{}", allocationNo);
        return Response.<MaterialStockAllocationDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("原料库存分配单-查询详情完成")
                .data(response)
                .build();
    }

    /**
     * 兼容内部按主键查询分配单详情。
     */
    @GetMapping("id/{id}")
    public Response<MaterialStockAllocationDetailResponse> detailById(@PathVariable("id") @NotNull Long id) {
        log.info("原料库存分配单-按ID查询原料库存分配单详情开始 id:{}", id);
        MaterialStockAllocationAggregate materialStockAllocationAggregate = materialStockAllocationService.queryById(id);
        MaterialStockAllocationDetailResponse response = MaterialStockAllocationAssembler.toDetailResponse(materialStockAllocationAggregate);
        log.info("原料库存分配单-按ID查询原料库存分配单详情完成 id:{}", id);
        return Response.<MaterialStockAllocationDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("原料库存分配单-按ID查询详情完成")
                .data(response)
                .build();
    }

    /**
     * 按分配明细逐条锁库。
     */
    @PostMapping("lock/{allocationNo}")
    public Response<Boolean> lock(@PathVariable("allocationNo") @NotBlank String allocationNo) {
        log.info("原料库存分配单-锁定原料库存分配单开始 allocationNo:{}", allocationNo);
        materialStockAllocationService.lock(allocationNo);
        log.info("原料库存分配单-锁定原料库存分配单完成 allocationNo:{}", allocationNo);

        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("原料库存分配单-锁定成功")
                .data(true)
                .build();
    }

    /**
     * 按分配明细逐条释放锁库。
     */
    @PostMapping("release/{allocationNo}")
    public Response<Boolean> release(@PathVariable("allocationNo") @NotBlank String allocationNo) {
        log.info("释放原料库存分配单开始 allocationNo:{}", allocationNo);
        materialStockAllocationService.release(allocationNo);
        log.info("原料库存分配单-释放原料库存分配单完成 allocationNo:{}", allocationNo);

        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("原料库存分配单-释放成功")
                .data(true)
                .build();
    }

    /**
     * 按分配明细逐条流水线出库。
     */
    @PostMapping("auto-outbound/{allocationNo}")
    public Response<Boolean> autoOutbound(@PathVariable("allocationNo") @NotBlank String allocationNo) {
        log.info("流水线原料出库开始 allocationNo:{}", allocationNo);
        throw new UnsupportedOperationException("待实现：原料库存分配单流水线出库");
    }
}
