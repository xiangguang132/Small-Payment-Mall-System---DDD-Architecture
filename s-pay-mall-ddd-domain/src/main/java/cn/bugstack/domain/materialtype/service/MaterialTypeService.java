package cn.bugstack.domain.materialtype.service;

import cn.bugstack.domain.materialtype.model.aggregate.MaterialTypeAggregate;
import cn.bugstack.domain.materialtype.model.vo.MaterialTypeStatusVO;
import cn.bugstack.domain.materialtype.repository.IMaterialTypeRepository;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class MaterialTypeService implements IMaterialTypeService {

    @Resource
    private IMaterialTypeRepository materialTypeRepository;

    @Override
    public Long addNewMaterialType(MaterialTypeAggregate materialType) {
        if (materialType == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "物料分类不能为空");
        }
        validateName(materialType.getName());
        validateTypeCode(materialType.getTypeCode());
        validateSort(materialType.getSort());
        validateStatus(materialType.getStatus());
        validateParentId(materialType.getParentId(), null);
        validateUniqueTypeCode(materialType.getTypeCode(), null);
        return materialTypeRepository.save(materialType);
    }

    @Override
    public void deleteMaterialTypeById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料分类id不能为空");
        }
        MaterialTypeAggregate current = materialTypeRepository.queryById(id);
        if (current == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "原料分类不存在");
        }

        long childCount = materialTypeRepository.countByParentId(id);
        if (childCount > 0) {
            throw new AppException(ResponseCode.CONFLICT, "原料分类存在子分类，不能删除");
        }

        long materialCount = materialTypeRepository.countByTypeId(id);
        if (materialCount > 0) {
            throw new AppException(ResponseCode.CONFLICT, "原料分类已被原料使用，不能删除");
        }

        materialTypeRepository.deleteById(id);
    }

    @Override
    public MaterialTypeAggregate queryMaterialTypeById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料分类id不能为空");
        }
        MaterialTypeAggregate materialType = materialTypeRepository.queryById(id);
        if (materialType == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "原料分类不存在");
        }
        return materialType;
    }

    @Override
    public void updateMaterialTypeById(MaterialTypeAggregate materialType) {
        if (materialType == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料分类不能为空");
        }
        if (materialType.getId() == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料分类id不能为空");
        }

        MaterialTypeAggregate current = materialTypeRepository.queryById(materialType.getId());
        if (current == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "原料分类不存在");
        }

        validateName(materialType.getName());
        validateTypeCode(materialType.getTypeCode());
        validateSort(materialType.getSort());
        validateStatus(materialType.getStatus());
        validateParentId(materialType.getParentId(), materialType.getId());
        validateUniqueTypeCode(materialType.getTypeCode(), materialType.getId());

        MaterialTypeAggregate updated = MaterialTypeAggregate.builder()
                .id(current.getId())
                .parentId(materialType.getParentId())
                .name(materialType.getName())
                .description(materialType.getDescription())
                .typeCode(materialType.getTypeCode())
                .sort(materialType.getSort())
                .status(materialType.getStatus())
                .isDel(current.getIsDel())
                .createTime(current.getCreateTime())
                .updateTime(LocalDateTime.now())
                .build();
        materialTypeRepository.updateById(updated);
    }

    @Override
    public MaterialTypeAggregate enableMaterialTypeById(Long id) {
        return updateMaterialTypeStatus(id, 1);
    }

    @Override
    public MaterialTypeAggregate disableMaterialTypeById(Long id) {
        return updateMaterialTypeStatus(id, 0);
    }

    private MaterialTypeAggregate updateMaterialTypeStatus(Long id, Integer status) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料分类id不能为空");
        }
        validateStatus(status);

        MaterialTypeAggregate current = materialTypeRepository.queryById(id);
        if (current == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "原料分类不存在");
        }
        if (status.equals(current.getStatus())) {
            return current;
        }

        MaterialTypeAggregate updated = MaterialTypeAggregate.builder()
                .id(current.getId())
                .parentId(current.getParentId())
                .name(current.getName())
                .description(current.getDescription())
                .typeCode(current.getTypeCode())
                .sort(current.getSort())
                .status(status)
                .isDel(current.getIsDel())
                .createTime(current.getCreateTime())
                .updateTime(LocalDateTime.now())
                .build();
        materialTypeRepository.updateById(updated);
        return updated;
    }

    private void validateStatus(Integer status) {
        if (!MaterialTypeStatusVO.isValid(status)) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料分类状态值非法");
        }
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料分类名称不能为空");
        }
    }

    private void validateTypeCode(String typeCode) {
        if (typeCode == null || typeCode.trim().isEmpty()) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料分类编码不能为空");
        }
    }

    private void validateParentId(Long parentId, Long currentId) {
        if (parentId == null || parentId == 0L) {
            return;
        }
        if (parentId < 0) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "父分类id不能小于0");
        }

        if (currentId != null && parentId.equals(currentId)) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "父分类不能是当前分类本身");
        }

        MaterialTypeAggregate parent = materialTypeRepository.queryById(parentId);
        if (parent == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "父分类不存在");
        }
        if (parent.getStatus() == null || parent.getStatus() != 1) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "父分类未启用，不能使用");
        }
    }

    private void validateUniqueTypeCode(String typeCode, Long currentId) {
        MaterialTypeAggregate existed = materialTypeRepository.queryByTypeCode(typeCode);
        if (existed == null) {
            return;
        }
        if (currentId == null || !currentId.equals(existed.getId())) {
            throw new AppException(ResponseCode.CONFLICT, "原料分类编码已存在");
        }
    }

    private void validateSort(Integer sort) {
        if (sort == null || sort < 0) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "排序值不能小于0");
        }
    }
}
