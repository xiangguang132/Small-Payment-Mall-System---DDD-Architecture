package cn.bugstack.trigger.assembler;


import cn.bugstack.api.response.materialstock.MaterialStockDetailResponse;
import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;

public class MaterialStockAssembler {

    private MaterialStockAssembler() {
    }

    public static MaterialStockDetailResponse toDetailResponse(MaterialStockAggregate materialStockAggregate) {
        if (null == materialStockAggregate) {
            return null;
        }
        MaterialStockDetailResponse response = new MaterialStockDetailResponse();
        response.setId(materialStockAggregate.getId());
        response.setMaterialId(materialStockAggregate.getMaterialId());
        response.setStorageAddress(materialStockAggregate.getStorageAddress());
        response.setAvailableQty(materialStockAggregate.getAvailableQty());
        response.setLockedQty(materialStockAggregate.getLockedQty());
        response.setTotalQty(materialStockAggregate.getTotalQty());
        response.setIsDel(materialStockAggregate.getIsDel());
        response.setCreateTime(materialStockAggregate.getCreateTime());
        response.setUpdateTime(materialStockAggregate.getUpdateTime());

        return response;
    }
}
