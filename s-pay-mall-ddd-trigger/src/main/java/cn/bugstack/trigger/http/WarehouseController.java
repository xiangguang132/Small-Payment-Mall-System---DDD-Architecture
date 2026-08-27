package cn.bugstack.trigger.http;

import cn.bugstack.api.request.warehouse.WarehouseAddRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.warehouse.WarehouseDetailResponse;
import cn.bugstack.domain.warehouse.model.aggregate.WarehouseAggregate;
import cn.bugstack.domain.warehouse.service.IWarehouseService;
import cn.bugstack.trigger.assembler.WarehouseAssembler;
import cn.bugstack.trigger.interceptor.RequireRole;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.enums.RoleEnum;
import cn.bugstack.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@CrossOrigin("*")
@Validated
@RequestMapping("/api/v1/warehouse")
@RequireRole({RoleEnum.ADMIN, RoleEnum.INVENTORY_MANAGER})
@Slf4j
public class WarehouseController {

    @Resource
    private IWarehouseService warehouseService;

    @PostMapping("add")
    public Response<Long> add(@Valid @RequestBody WarehouseAddRequest request) {
        log.info("新增仓库开始 request:{}", request);
        WarehouseAggregate warehouse = WarehouseAssembler.toAggregate(request);
        Long id = warehouseService.addWarehouse(warehouse);
        log.info("新增仓库完成 id:{}", id);
        return Response.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(id)
                .build();
    }

    @GetMapping("{id}")
    public Response<WarehouseDetailResponse> detail(@PathVariable Long id) {
        log.info("查询仓库详情开始 id:{}", id);
        WarehouseAggregate warehouse = warehouseService.queryWarehouseById(id);
        WarehouseDetailResponse response = WarehouseAssembler.toDetailResponse(warehouse);
        log.info("查询仓库详情完成 id:{}", id);
        return Response.<WarehouseDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    @PutMapping("{id}")
    public Response<WarehouseDetailResponse> update(@PathVariable Long id, @Valid @RequestBody WarehouseAddRequest request) {
        log.info("更新仓库开始 id:{} request:{}", id, request);
        WarehouseAggregate current = warehouseService.queryWarehouseById(id);
        if (current == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "仓库不存在");
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
        log.info("更新仓库完成 id:{}", id);
        return Response.<WarehouseDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    @DeleteMapping("{id}")
    public Response<Boolean> delete(@PathVariable Long id) {
        log.info("删除仓库开始 id:{}", id);
        warehouseService.deleteWarehouseById(id);
        log.info("删除仓库完成 id:{}", id);
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(true)
                .build();
    }
}
