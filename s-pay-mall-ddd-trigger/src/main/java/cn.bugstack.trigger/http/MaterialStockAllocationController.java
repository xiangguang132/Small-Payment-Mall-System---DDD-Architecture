package cn.bugstack.trigger.http;

import cn.bugstack.api.request.materialstockallocation.MaterialStockAllocationCreateRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.materialstockallocation.MaterialStockAllocationDetailResponse;
import cn.bugstack.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@RestController
@CrossOrigin("*")
@Validated
@RequestMapping("/api/v1/material-stock-allocation")
@Slf4j
public class MaterialStockAllocationController {

    /**
     * 创建一张跨库位分配单。
     * 当前阶段先把 Controller 骨架搭起来，后续接入 service 后再补业务实现。
     */
    @PostMapping("create")
    public Response<String> create(@Valid @RequestBody MaterialStockAllocationCreateRequest request) {
        log.info("创建原料库存分配单开始 request:{}", request);
        throw new UnsupportedOperationException("待实现：创建原料库存分配单");
    }

    /**
     * 按分配单号查询分配详情，返回主单和明细。
     */
    @GetMapping("{allocationNo}")
    public Response<MaterialStockAllocationDetailResponse> detail(@PathVariable("allocationNo") @NotBlank String allocationNo) {
        log.info("查询原料库存分配单详情开始 allocationNo:{}", allocationNo);
        throw new UnsupportedOperationException("待实现：查询原料库存分配单详情");
    }

    /**
     * 兼容内部按主键查询分配单详情。
     */
    @GetMapping("id/{id}")
    public Response<MaterialStockAllocationDetailResponse> detailById(@PathVariable("id") @NotNull Long id) {
        log.info("按ID查询原料库存分配单详情开始 id:{}", id);
        throw new UnsupportedOperationException("待实现：按ID查询原料库存分配单详情");
    }

    /**
     * 按分配明细逐条锁库。
     */
    @PostMapping("lock/{allocationNo}")
    public Response<Boolean> lock(@PathVariable("allocationNo") @NotBlank String allocationNo) {
        log.info("锁定原料库存分配单开始 allocationNo:{}", allocationNo);
        throw new UnsupportedOperationException("待实现：锁定原料库存分配单");
    }

    /**
     * 按分配明细逐条释放锁库。
     */
    @PostMapping("release/{allocationNo}")
    public Response<Boolean> release(@PathVariable("allocationNo") @NotBlank String allocationNo) {
        log.info("释放原料库存分配单开始 allocationNo:{}", allocationNo);
        throw new UnsupportedOperationException("待实现：释放原料库存分配单");
    }

    /**
     * 按分配明细逐条流水线出库。
     */
    @PostMapping("auto-outbound/{allocationNo}")
    public Response<Boolean> autoOutbound(@PathVariable("allocationNo") @NotBlank String allocationNo) {
        log.info("流水线原料出库开始 allocationNo:{}", allocationNo);
        throw new UnsupportedOperationException("待实现：原料库存分配单流水线出库");
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public Response<Boolean> handleUnsupportedOperationException(UnsupportedOperationException e) {
        return Response.<Boolean>builder()
                .code(ResponseCode.UN_ERROR.getCode())
                .info(e.getMessage())
                .data(false)
                .build();
    }
}
