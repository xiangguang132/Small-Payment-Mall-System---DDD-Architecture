package cn.bugstack.domain.materialtype.repository;

import cn.bugstack.domain.materialtype.model.aggregate.MaterialTypeAggregate;

public interface IMaterialTypeRepository {

    Long save(MaterialTypeAggregate materialType);

    void deleteById(Long id);

    MaterialTypeAggregate queryById(Long id);

    MaterialTypeAggregate queryByTypeCode(String typeCode);

    void updateById(MaterialTypeAggregate materialType);

    long countByParentId(Long parentId);

    long countByTypeId(Long typeId);
}
