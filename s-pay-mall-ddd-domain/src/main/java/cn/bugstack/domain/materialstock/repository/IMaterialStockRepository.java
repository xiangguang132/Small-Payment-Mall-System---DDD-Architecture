package cn.bugstack.domain.materialstock.repository;

import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;

public interface IMaterialStockRepository {

    MaterialStockAggregate queryById(Long id);

    MaterialStockAggregate queryByMaterialIdAndStorageAddress(Long materialId, String storageAddress);

    Long save(MaterialStockAggregate stock);

    void updateById(MaterialStockAggregate stock);
}
