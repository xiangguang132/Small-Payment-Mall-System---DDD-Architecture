package cn.bugstack.trigger.assembler;

import cn.bugstack.api.response.warehousestock.StockDetailResponse;
import cn.bugstack.domain.warehousestock.model.aggregate.StockAggregate;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;

public class StockAssembler {

    private StockAssembler() {
    }

    public static StockDetailResponse toDetailResponse(StockAggregate stock) {
        if (stock == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "库存不存在");
        }
        StockDetailResponse response = new StockDetailResponse();
        response.setId(stock.getId());
        response.setWarehouseId(stock.getWarehouseId());
        response.setProductId(stock.getProductId());
        response.setAvailableQty(stock.getAvailableQty());
        response.setLockedQty(stock.getLockedQty());
        response.setTotalQty(stock.getTotalQty());
        response.setIsDel(stock.getIsDel());
        response.setCreateTime(stock.getCreateTime());
        response.setUpdateTime(stock.getUpdateTime());
        return response;
    }
}
