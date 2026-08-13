package cn.bugstack.trigger.assembler;


import cn.bugstack.api.response.materialstock.MaterialStockDetailResponse;
import cn.bugstack.api.response.materialstock.MaterialStockManualOutboundResponse;
import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;

public class MaterialStockAssembler {

    private MaterialStockAssembler() {
    }

    /**
     * 转化为详情响应体
     * @param materialStockAggregate
     * @return
     */
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

    /**
     * 转化为人工类出库响应体
     * @param stock
     * @return
     */
    public static MaterialStockManualOutboundResponse toManualOutboundResponse(MaterialStockAggregate stock) {
        if (null == stock) {
            return null;
        }
        MaterialStockManualOutboundResponse response = new MaterialStockManualOutboundResponse();
        response.setStorageAddress(stock.getStorageAddress());
        response.setAvailableQty(stock.getAvailableQty());
        response.setLockedQty(stock.getLockedQty());
        response.setTotalQty(stock.getTotalQty());
        response.setUpdateTime(stock.getUpdateTime());
        response.setCreateTime(stock.getCreateTime());

        return response;
    }
}
