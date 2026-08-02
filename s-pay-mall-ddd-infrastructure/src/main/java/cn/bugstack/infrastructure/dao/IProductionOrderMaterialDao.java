package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.ProductionOrderMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IProductionOrderMaterialDao {

    void insertBatch(@Param("list") List<ProductionOrderMaterial> list);

    List<ProductionOrderMaterial> queryByProductionOrderId(@Param("productionOrderId") Long productionOrderId);
}
