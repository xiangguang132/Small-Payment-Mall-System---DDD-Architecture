package cn.bugstack.domain.materialtype.repository;

import cn.bugstack.domain.materialtype.model.aggregate.MaterialTypeAggregate;

public interface IMaterialTypeRepository {

    Long save(MaterialTypeAggregate materialType);
}
