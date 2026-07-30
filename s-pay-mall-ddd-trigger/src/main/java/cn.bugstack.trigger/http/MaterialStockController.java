package cn.bugstack.trigger.http;

import cn.bugstack.api.request.materialstock.MaterialStockInboundRequest;
import cn.bugstack.api.request.materialstock.MaterialStockQuantityRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.domain.materialstock.service.IMaterialStockService;
import cn.bugstack.types.enums.ResponseCode;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/material-stock")
public class MaterialStockController {

    @Resource
    private IMaterialStockService materialStockService;

    /**
     * 原料入库
     * @param request
     * @return
     */
    @PostMapping("inbound")
    public Response<Boolean> inbound(@Valid @RequestBody MaterialStockInboundRequest request) {
        materialStockService.inbound(
                request.getMaterialId(),
                request.getStorageAddress(),
                request.getInboundQty(),
                request.getReason()
        );

        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(true)
                .build();
    }

    @PostMapping("lock/{id}")
    public Response<Boolean> lock(@PathVariable("id") Long id,
                                  @Valid @RequestBody MaterialStockQuantityRequest request
                                  ) {
        materialStockService.lock(id, request.getQuantity());
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(true)
                .build();
    }

}
