package cn.bugstack.trigger.http;

import cn.bugstack.api.request.materialstock.MaterialStockInboundRequest;
import cn.bugstack.api.request.materialstock.MaterialStockQuantityRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.materialstock.MaterialStockDetailResponse;
import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;
import cn.bugstack.domain.materialstock.service.IMaterialStockService;
import cn.bugstack.trigger.assembler.MaterialStockAssembler;
import cn.bugstack.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/material-stock")
@Slf4j
public class MaterialStockController {

    @Resource
    private IMaterialStockService materialStockService;

    @GetMapping("{id}")
    public Response<MaterialStockDetailResponse> detail(@PathVariable @NotBlank Long id) {
        log.info("查询原料库存详情开始 id:{}", id);
        if (id == null) {
            throw new IllegalArgumentException("原料库存的id不能为空");
        }
        MaterialStockAggregate materialStockAggregate = materialStockService.queryMaterialStockById(id);
        MaterialStockDetailResponse response = MaterialStockAssembler.toDetailResponse(materialStockAggregate);
        log.info("查询原料库存详情完成 id:{}", id);
        return Response.<MaterialStockDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    /**
     * 原料入库
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
                .info(ResponseCode.SUCCESS.getInfo())
                .data(true)
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
                .info(ResponseCode.SUCCESS.getInfo())
                .data(true)
                .build();
    }

}
