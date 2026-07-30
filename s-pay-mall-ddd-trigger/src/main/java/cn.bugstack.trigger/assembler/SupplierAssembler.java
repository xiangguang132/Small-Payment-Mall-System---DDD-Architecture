package cn.bugstack.trigger.assembler;

import cn.bugstack.api.request.supplier.SupplierAddRequest;
import cn.bugstack.api.response.supplier.SupplierDetailResponse;
import cn.bugstack.domain.supplier.model.aggregate.SupplierAggregate;

public class SupplierAssembler {

    private SupplierAssembler() {
    }

    public static SupplierAggregate toAggregate(SupplierAddRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("供应商信息不能为空");
        }
        return SupplierAggregate.create(
                trim(request.getSupplierCode()),
                trim(request.getName()),
                trim(request.getContactName()),
                trim(request.getContactPhone()),
                trim(request.getAddress()),
                request.getStatus()
        );
    }

    public static SupplierDetailResponse toDetailResponse(SupplierAggregate supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("暂无相关供应商详情");
        }
        SupplierDetailResponse response = new SupplierDetailResponse();
        response.setId(supplier.getId());
        response.setSupplierCode(supplier.getSupplierCode());
        response.setName(supplier.getName());
        response.setContactName(supplier.getContactName());
        response.setContactPhone(supplier.getContactPhone());
        response.setAddress(supplier.getAddress());
        response.setStatus(supplier.getStatus());
        response.setIsDel(supplier.getIsDel());
        response.setCreateTime(supplier.getCreateTime());
        response.setUpdateTime(supplier.getUpdateTime());
        return response;
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
