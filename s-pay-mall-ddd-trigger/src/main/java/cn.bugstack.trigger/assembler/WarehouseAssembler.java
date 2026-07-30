package cn.bugstack.trigger.assembler;

import cn.bugstack.api.request.warehouse.WarehouseAddRequest;
import cn.bugstack.api.response.warehouse.WarehouseDetailResponse;
import cn.bugstack.domain.warehouse.model.aggregate.WarehouseAggregate;

public class WarehouseAssembler {

    private WarehouseAssembler() {
    }

    public static WarehouseAggregate toAggregate(WarehouseAddRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("仓库信息不能为空");
        }
        return WarehouseAggregate.create(
                trim(request.getWarehouseCode()),
                trim(request.getName()),
                request.getType(),
                trim(request.getAddress()),
                trim(request.getContactName()),
                trim(request.getContactPhone()),
                request.getStatus()
        );
    }

    public static WarehouseDetailResponse toDetailResponse(WarehouseAggregate warehouse) {
        if (warehouse == null) {
            throw new IllegalArgumentException("暂无相关仓库详情");
        }
        WarehouseDetailResponse response = new WarehouseDetailResponse();
        response.setId(warehouse.getId());
        response.setWarehouseCode(warehouse.getWarehouseCode());
        response.setName(warehouse.getName());
        response.setType(warehouse.getType());
        response.setAddress(warehouse.getAddress());
        response.setContactName(warehouse.getContactName());
        response.setContactPhone(warehouse.getContactPhone());
        response.setStatus(warehouse.getStatus());
        response.setIsDel(warehouse.getIsDel());
        response.setCreateTime(warehouse.getCreateTime());
        response.setUpdateTime(warehouse.getUpdateTime());
        return response;
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
