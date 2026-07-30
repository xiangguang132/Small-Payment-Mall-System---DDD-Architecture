package cn.bugstack.domain.materialtype.service;

import cn.bugstack.domain.materialtype.model.aggregate.MaterialTypeAggregate;

public interface IMaterialTypeService {

    Long addNewMaterialType(MaterialTypeAggregate materialType);
}
