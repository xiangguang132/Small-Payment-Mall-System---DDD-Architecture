package cn.bugstack.trigger.http;

import cn.bugstack.api.request.materialstockallocation.MaterialStockAllocationCreateRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.materialstockallocation.MaterialStockAllocationDetailResponse;
import cn.bugstack.api.response.materialstockallocation.MaterialStockAllocationStatusResponse;
import cn.bugstack.domain.materialstockallocation.model.aggregate.MaterialStockAllocationAggregate;
import cn.bugstack.domain.materialstockallocation.service.IMaterialStockAllocationService;
import cn.bugstack.domain.production.model.vo.ProductionOrderMaterialVO;
import cn.bugstack.trigger.assembler.MaterialStockAllocationAssembler;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

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
                request.getMaterialId(),
                request.getQuantity(),
                request.getReason()
        );

        log.info("创建原料库存分配单完成 allocationNo:{} materialId:{} quantity:{}", allocationNo, request.getMaterialId(), request.getQuantity());
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
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "分配单号不能为空");
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
        materialStockAllocationService.autoOutbound(allocationNo);
        log.info("流水线原料出库完成 allocationNo:{}", allocationNo);

        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("原料库存分配单-流水线出库成功")
                .data(true)
                .build();
    }

    @GetMapping("status/{status}")
    public Response<List<MaterialStockAllocationStatusResponse>> queryByStatus(
            @PathVariable("status") @NotNull Integer status,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {

        log.info("按状态查询原料库存分配单开始 status:{} pageNo:{} pageSize:{}",
                status, pageNo, pageSize);

        List<MaterialStockAllocationAggregate> aggregates =
                materialStockAllocationService.queryByStatus(status, pageNo, pageSize);

        List<MaterialStockAllocationStatusResponse> responses = aggregates.stream()
                .map(MaterialStockAllocationAssembler::toStatusResponse)
                .collect(Collectors.toList());

        log.info("按状态查询原料库存分配单完成 status:{} count:{}",
                status, responses.size());

        return Response.<List<MaterialStockAllocationStatusResponse>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("原料库存分配单-按状态查询完成")
                .data(responses)
                .build();
    }
}
