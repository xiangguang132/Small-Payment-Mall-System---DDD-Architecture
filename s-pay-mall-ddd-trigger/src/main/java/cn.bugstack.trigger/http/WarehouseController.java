package cn.bugstack.trigger.http;

import cn.bugstack.api.request.warehouse.WarehouseAddRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.warehouse.WarehouseDetailResponse;
import cn.bugstack.domain.warehouse.model.aggregate.WarehouseAggregate;
import cn.bugstack.domain.warehouse.service.IWarehouseService;
import cn.bugstack.trigger.assembler.WarehouseAssembler;
import cn.bugstack.types.enums.ResponseCode;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@CrossOrigin("*")
@Validated
@RequestMapping("/api/v1/warehouse")
public class WarehouseController {

    @Resource
    private IWarehouseService warehouseService;

    @PostMapping("add")
    public Response<Long> add(@Valid @RequestBody WarehouseAddRequest request) {
        WarehouseAggregate warehouse = WarehouseAssembler.toAggregate(request);
        Long id = warehouseService.addWarehouse(warehouse);
        return Response.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(id)
                .build();
    }

    @GetMapping("{id}")
    public Response<WarehouseDetailResponse> detail(@PathVariable Long id) {
        WarehouseAggregate warehouse = warehouseService.queryWarehouseById(id);
        WarehouseDetailResponse response = WarehouseAssembler.toDetailResponse(warehouse);
        return Response.<WarehouseDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    @PutMapping("{id}")
    public Response<WarehouseDetailResponse> update(@PathVariable Long id, @Valid @RequestBody WarehouseAddRequest request) {
        WarehouseAggregate current = warehouseService.queryWarehouseById(id);
        if (current == null) {
            throw new IllegalArgumentException("仓库不存在");
        }
        WarehouseAggregate updated = WarehouseAggregate.builder()
                .id(current.getId())
                .warehouseCode(request.getWarehouseCode() != null ? request.getWarehouseCode().trim() : current.getWarehouseCode())
                .name(request.getName() != null ? request.getName().trim() : current.getName())
                .type(request.getType() != null ? request.getType() : current.getType())
                .address(request.getAddress() != null ? request.getAddress().trim() : current.getAddress())
                .contactName(request.getContactName() != null ? request.getContactName().trim() : current.getContactName())
                .contactPhone(request.getContactPhone() != null ? request.getContactPhone().trim() : current.getContactPhone())
                .status(request.getStatus() != null ? request.getStatus() : current.getStatus())
                .isDel(current.getIsDel())
                .createTime(current.getCreateTime())
                .build();
        warehouseService.updateWarehouseById(updated);

        WarehouseAggregate refreshed = warehouseService.queryWarehouseById(id);
        WarehouseDetailResponse response = WarehouseAssembler.toDetailResponse(refreshed);
        return Response.<WarehouseDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    @DeleteMapping("{id}")
    public Response<Boolean> delete(@PathVariable Long id) {
        warehouseService.deleteWarehouseById(id);
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(true)
                .build();
    }
}
