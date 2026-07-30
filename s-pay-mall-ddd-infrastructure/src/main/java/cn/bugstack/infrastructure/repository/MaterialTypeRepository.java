package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.materialtype.model.aggregate.MaterialTypeAggregate;
import cn.bugstack.domain.materialtype.repository.IMaterialTypeRepository;
import cn.bugstack.infrastructure.dao.IMaterialTypeDao;
import cn.bugstack.infrastructure.dao.po.MaterialType;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class MaterialTypeRepository implements IMaterialTypeRepository {

    @Resource
    private IMaterialTypeDao materialTypeDao;

    @Override
    public Long save(MaterialTypeAggregate materialTypeAggregate) {
        if (materialTypeAggregate == null) {
            throw new IllegalArgumentException("原料分类不能为空");
        }
        MaterialType materialType = MaterialType.builder()
                .parentId(materialTypeAggregate.getParentId() == null ? 0L : materialTypeAggregate.getParentId())
                .name(materialTypeAggregate.getName())
                .description(materialTypeAggregate.getDescription())
                .typeCode(materialTypeAggregate.getTypeCode())
                .sort(materialTypeAggregate.getSort() == null ? 0 : materialTypeAggregate.getSort())
                .status(materialTypeAggregate.getStatus())
                .isDel(materialTypeAggregate.getIsDel())
                .createTime(materialTypeAggregate.getCreateTime())
                .updateTime(materialTypeAggregate.getUpdateTime())
                .build();
        materialTypeDao.insert(materialType);
        return materialType.getId();
    }
}
