package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.MaterialStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IMaterialStockDao {

    void insert(MaterialStock stock);

    MaterialStock queryById(@Param("id") Long id);

    MaterialStock queryByMaterialIdAndStorageAddress(@Param("materialId") Long materialId,
                                                      @Param("storageAddress") String storageAddress);

    void update(MaterialStock stock);
}
