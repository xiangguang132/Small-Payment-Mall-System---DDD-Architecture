package cn.bugstack.domain.material.service;

import cn.bugstack.domain.material.model.aggregate.MaterialAggregate;

public interface IMaterialService {

    Long addNewMaterial(MaterialAggregate material);

    void deleteMaterialById(Long id);

    MaterialAggregate queryMaterialById(Long id);

    void validateMaterialEnabled(Long id);

    void updateMaterialById(MaterialAggregate material);
}
