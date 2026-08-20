package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.materialtype.model.aggregate.MaterialTypeAggregate;
import cn.bugstack.domain.materialtype.repository.IMaterialTypeRepository;
import cn.bugstack.infrastructure.adapter.repository.AbstractRepository;
import cn.bugstack.infrastructure.dao.IMaterialDao;
import cn.bugstack.infrastructure.dao.IMaterialTypeDao;
import cn.bugstack.infrastructure.dao.po.material.MaterialType;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class MaterialTypeRepository extends AbstractRepository implements IMaterialTypeRepository {

    @Resource
    private IMaterialDao materialDao;

    @Resource
    private IMaterialTypeDao materialTypeDao;

    @Override
    public Long save(MaterialTypeAggregate materialTypeAggregate) {
        if (materialTypeAggregate == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料分类不能为空");
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

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料分类id不能为空");
        }
        MaterialTypeAggregate current = queryById(id);

        materialTypeDao.deleteById(id);

        redisService.remove(cacheKeyById(id));
        if (current != null) {
            redisService.remove(cacheKeyByTypeCode(current.getTypeCode()));
        }
    }

    @Override
    public MaterialTypeAggregate queryById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料分类id不能为空");
        }
        return getFromCacheOrDb(
                cacheKeyById(id),
                () -> {
                    MaterialType materialType = materialTypeDao.queryById(id);
                    if (materialType == null) {
                        return null;
                    }
                    return MaterialTypeAggregate.builder()
                            .id(materialType.getId())
                            .parentId(materialType.getParentId())
                            .name(materialType.getName())
                            .description(materialType.getDescription())
                            .typeCode(materialType.getTypeCode())
                            .sort(materialType.getSort())
                            .status(materialType.getStatus())
                            .isDel(materialType.getIsDel())
                            .createTime(materialType.getCreateTime())
                            .updateTime(materialType.getUpdateTime())
                            .build();
                }
        );
    }

    @Override
    public MaterialTypeAggregate queryByTypeCode(String typeCode) {
        if (typeCode == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料分类编码不能为空");
        }
        return getFromCacheOrDb(
                cacheKeyByTypeCode(typeCode),
                () -> {
                    MaterialType materialType = materialTypeDao.queryByTypeCode(typeCode);
                    if (materialType == null) {
                        return null;
                    }
                    return MaterialTypeAggregate.builder()
                            .id(materialType.getId())
                            .parentId(materialType.getParentId())
                            .name(materialType.getName())
                            .description(materialType.getDescription())
                            .typeCode(materialType.getTypeCode())
                            .sort(materialType.getSort())
                            .status(materialType.getStatus())
                            .isDel(materialType.getIsDel())
                            .createTime(materialType.getCreateTime())
                            .updateTime(materialType.getUpdateTime())
                            .build();
                }
        );
    }

    @Override
    public void updateById(MaterialTypeAggregate materialTypeAggregate) {
        if (materialTypeAggregate == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料分类不能为空");
        }
        if (materialTypeAggregate.getId() == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料分类id不能为空");
        }
        MaterialType materialType = MaterialType.builder()
                .id(materialTypeAggregate.getId())
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
        materialTypeDao.update(materialType);

        redisService.remove(cacheKeyById(materialTypeAggregate.getId()));
        if (materialTypeAggregate.getTypeCode() != null) {
            redisService.remove(cacheKeyByTypeCode(materialTypeAggregate.getTypeCode()));
        }
    }

    @Override
    public long countByParentId(Long parentId) {
        if (parentId == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "父分类id不能为空");
        }
        return materialTypeDao.countByParentId(parentId);
    }

    @Override
    public long countByTypeId(Long typeId) {
        if (typeId == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料分类id不能为空");
        }
        return materialDao.countByTypeId(typeId);
    }

    private String cacheKeyById(Long id) {
        return "s-pay-mall:material-type:id:" + id;
    }

    private String cacheKeyByTypeCode(String typeCode) {
        if (typeCode == null) {
            return null;
        }
        return "s-pay-mall:material-type:code:" + typeCode;
    }
}
