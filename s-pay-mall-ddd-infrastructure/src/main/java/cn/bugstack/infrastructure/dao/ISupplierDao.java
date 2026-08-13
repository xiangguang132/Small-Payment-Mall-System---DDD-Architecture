package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.supplier.Supplier;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ISupplierDao {

    void insert(Supplier supplier);

    void deleteById(@Param("id") Long id);

    Supplier queryById(@Param("id") Long id);

    void update(Supplier supplier);
}
