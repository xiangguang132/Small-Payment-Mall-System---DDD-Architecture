package cn.bugstack.trigger.assembler;

import cn.bugstack.api.response.materialstockallocation.MaterialStockAllocationDetailResponse;
import cn.bugstack.api.response.materialstockallocation.MaterialStockAllocationItemResponse;
import cn.bugstack.domain.materialstockallocation.model.aggregate.MaterialStockAllocationAggregate;
import cn.bugstack.domain.materialstockallocation.model.vo.MaterialStockAllocationItemVO;

import java.util.List;
import java.util.stream.Collectors;

public class MaterialStockAllocationAssembler {

    private MaterialStockAllocationAssembler() {
    }

    public static MaterialStockAllocationDetailResponse toDetailResponse(MaterialStockAllocationAggregate aggregate) {
        if (aggregate == null) {
            return null;
        }
        return MaterialStockAllocationDetailResponse.builder()
                .id(aggregate.getId())
                .allocationNo(aggregate.getAllocationNo())
                .materialId(aggregate.getMaterialId())
                .requestStockId(aggregate.getRequestStockId())
                .requestQty(aggregate.getRequestQty())
                .lockedQty(aggregate.getLockedQty())
                .outboundQty(aggregate.getOutboundQty())
                .releasedQty(aggregate.getReleasedQty())
                .status(aggregate.getStatus())
                .reason(aggregate.getReason())
                .createTime(aggregate.getCreateTime())
                .updateTime(aggregate.getUpdateTime())
                .items(toItemResponses(aggregate.getItems()))
                .build();
    }

    private static List<MaterialStockAllocationItemResponse> toItemResponses(List<MaterialStockAllocationItemVO> items) {
        if (items == null) {
            return null;
        }
        return items.stream()
                .map(MaterialStockAllocationAssembler::toItemResponse)
                .collect(Collectors.toList());
    }

    private static MaterialStockAllocationItemResponse toItemResponse(MaterialStockAllocationItemVO item) {
        if (item == null) {
            return null;
        }
        return MaterialStockAllocationItemResponse.builder()
                .stockId(item.getStockId())
                .materialId(item.getMaterialId())
                .storageAddress(item.getStorageAddress())
                .allocateQty(item.getAllocateQty())
                .lockedQty(item.getLockedQty())
                .outboundQty(item.getOutboundQty())
                .releasedQty(item.getReleasedQty())
                .sortNo(item.getSortNo())
                .status(item.getStatus())
                .build();
    }
}
