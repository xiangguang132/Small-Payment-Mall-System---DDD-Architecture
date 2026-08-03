package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.material.model.aggregate.MaterialAggregate;
import cn.bugstack.domain.material.repository.IMaterialRepository;
import cn.bugstack.infrastructure.dao.IMaterialDao;
import cn.bugstack.infrastructure.dao.po.Material;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class MaterialRepository implements IMaterialRepository {

    @Resource
    private IMaterialDao materialDao;

    @Override
    public Long save(MaterialAggregate materialAggregate) {
        if (materialAggregate == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料信息不能为空");
        }
        Material material = Material.builder()
                .materialCode(materialAggregate.getMaterialCode())
                .name(materialAggregate.getName())
                .typeId(materialAggregate.getTypeId())
                .unit(materialAggregate.getUnit())
                .description(materialAggregate.getDescription())
                .status(materialAggregate.getStatus())
                .isDel(materialAggregate.getIsDel())
                .createTime(materialAggregate.getCreateTime())
                .updateTime(materialAggregate.getUpdateTime())
                .build();
        materialDao.insert(material);
        return material.getId();
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料id不能为空");
        }
        materialDao.deleteById(id);
    }

    @Override
    public MaterialAggregate queryById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料id不能为空");
        }
        Material material = materialDao.queryById(id);
        if (material == null) {
            return null;
        }
        return toAggregate(material);
    }

    @Override
    public MaterialAggregate queryByMaterialCode(String materialCode) {
        if (materialCode == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料编码不能为空");
        }
        Material material = materialDao.queryByMaterialCode(materialCode);
        if (material == null) {
            return null;
        }
        return toAggregate(material);
    }

    @Override
    public void updateById(MaterialAggregate materialAggregate) {
        if (materialAggregate == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料信息不能为空");
        }
        if (materialAggregate.getId() == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料id不能为空");
        }
        Material material = Material.builder()
                .id(materialAggregate.getId())
                .materialCode(materialAggregate.getMaterialCode())
                .name(materialAggregate.getName())
                .typeId(materialAggregate.getTypeId())
                .unit(materialAggregate.getUnit())
                .description(materialAggregate.getDescription())
                .status(materialAggregate.getStatus())
                .isDel(materialAggregate.getIsDel())
                .createTime(materialAggregate.getCreateTime())
                .updateTime(materialAggregate.getUpdateTime())
                .build();
        materialDao.update(material);
    }

    @Override
    public long countByTypeId(Long typeId) {
        if (typeId == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料类型id不能为空");
        }
        return materialDao.countByTypeId(typeId);
    }

    private MaterialAggregate toAggregate(Material material) {
        return MaterialAggregate.builder()
                .id(material.getId())
                .materialCode(material.getMaterialCode())
                .name(material.getName())
                .typeId(material.getTypeId())
                .typeName(material.getTypeName())
                .typeDescription(material.getTypeDescription())
                .typeCode(material.getTypeCode())
                .unit(material.getUnit())
                .description(material.getDescription())
                .status(material.getStatus())
                .isDel(material.getIsDel())
                .createTime(material.getCreateTime())
                .updateTime(material.getUpdateTime())
                .build();
    }
}
