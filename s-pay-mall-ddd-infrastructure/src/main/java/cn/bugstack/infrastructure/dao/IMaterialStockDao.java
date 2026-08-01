package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.MaterialStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IMaterialStockDao {

    MaterialStock queryById(@Param("id") Long id);

    void insert(MaterialStock stock);

    MaterialStock queryByMaterialIdAndStorageAddress(@Param("materialId") Long materialId,
                                                     @Param("storageAddress") String storageAddress);

    List<MaterialStock> queryAvailableByMaterialId(@Param("materialId") Long materialId);

    List<MaterialStock> queryAvailableByMaterialIdExcludeStockId(@Param("materialId") Long materialId,
                                                                 @Param("excludeStockId") Long excludeStockId);

    void update(MaterialStock stock);

}
