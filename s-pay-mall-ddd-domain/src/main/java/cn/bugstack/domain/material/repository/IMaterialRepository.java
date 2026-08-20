package cn.bugstack.domain.material.repository;

import cn.bugstack.domain.material.model.aggregate.MaterialAggregate;

public interface IMaterialRepository {

    Long save(MaterialAggregate materialAggregate);

    void deleteById(Long id);

    MaterialAggregate queryById(Long materialId);

    MaterialAggregate queryByMaterialCode(String materialCode);

    void updateById(MaterialAggregate materialAggregate);

    long countByTypeId(Long typeId);
}
