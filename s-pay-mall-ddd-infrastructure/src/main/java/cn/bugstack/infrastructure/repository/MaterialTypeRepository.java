package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.materialtype.model.aggregate.MaterialTypeAggregate;
import cn.bugstack.domain.materialtype.repository.IMaterialTypeRepository;
import cn.bugstack.infrastructure.config.RedisCacheService;
import cn.bugstack.infrastructure.dao.IMaterialDao;
import cn.bugstack.infrastructure.dao.IMaterialTypeDao;
import cn.bugstack.infrastructure.dao.po.MaterialType;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class MaterialTypeRepository implements IMaterialTypeRepository {

    @Resource
    private IMaterialDao materialDao;

    @Resource
    private IMaterialTypeDao materialTypeDao;

    @Resource
    private RedisCacheService redisCacheService;

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

        redisCacheService.delete(
                cacheKeyById(id),
                current == null ? null : cacheKeyByTypeCode(current.getTypeCode())
        );
    }

    @Override
    public MaterialTypeAggregate queryById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料分类id不能为空");
        }
        String cacheKey = cacheKeyById(id);
        MaterialTypeAggregate cached = redisCacheService.get(cacheKey, MaterialTypeAggregate.class);
        if (cached != null) {
            return cached;
        }
        MaterialType materialType = materialTypeDao.queryById(id);
        if (materialType == null) {
            return null;
        }
        MaterialTypeAggregate aggregate = MaterialTypeAggregate.builder()
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
        redisCacheService.set(cacheKey, aggregate);
        return aggregate;
    }

    @Override
    public MaterialTypeAggregate queryByTypeCode(String typeCode) {
        if (typeCode == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料分类编码不能为空");
        }
        String cacheKey = cacheKeyByTypeCode(typeCode);
        MaterialTypeAggregate cached = redisCacheService.get(cacheKey, MaterialTypeAggregate.class);
        if (cached != null) {
            return cached;
        }

        MaterialType materialType = materialTypeDao.queryByTypeCode(typeCode);
        if (materialType == null) {
            return null;
        }

        MaterialTypeAggregate aggregate = MaterialTypeAggregate.builder()
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

        redisCacheService.set(cacheKey, aggregate);
        return aggregate;
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

        redisCacheService.delete(
                cacheKeyById(materialTypeAggregate.getId()),
                materialTypeAggregate.getTypeCode() == null ? null : cacheKeyByTypeCode(materialTypeAggregate.getTypeCode())
        );
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
