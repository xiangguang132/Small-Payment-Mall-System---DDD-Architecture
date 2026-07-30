package cn.bugstack.trigger.assembler;

import cn.bugstack.api.response.materialstock.MaterialStockDetailResponse;
import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;

public class MaterialStockAssembler {

    private MaterialStockAssembler() {
    }

    public static MaterialStockDetailResponse toDetailResponse(MaterialStockAggregate stock) {
        if (stock == null) {
            throw new IllegalArgumentException("原料库存不存在");
        }
        MaterialStockDetailResponse response = new MaterialStockDetailResponse();
        response.setId(stock.getId());
        response.setMaterialId(stock.getMaterialId());
        response.setStorageAddress(stock.getStorageAddress());
        response.setAvailableQty(stock.getAvailableQty());
        response.setLockedQty(stock.getLockedQty());
        response.setTotalQty(stock.getTotalQty());
        response.setIsDel(stock.getIsDel());
        response.setCreateTime(stock.getCreateTime());
        response.setUpdateTime(stock.getUpdateTime());
        return response;
    }
}
