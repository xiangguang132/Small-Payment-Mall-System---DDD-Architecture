package cn.bugstack.domain.materialtype.service;

import cn.bugstack.domain.materialtype.model.aggregate.MaterialTypeAggregate;

public interface IMaterialTypeService {

    Long addNewMaterialType(MaterialTypeAggregate materialType);

    void deleteMaterialTypeById(Long id);

    MaterialTypeAggregate queryMaterialTypeById(Long id);

    void updateMaterialTypeById(MaterialTypeAggregate materialType);

    MaterialTypeAggregate enableMaterialTypeById(Long id);

    MaterialTypeAggregate disableMaterialTypeById(Long id);
}
