package cn.bugstack.domain.material.service;

import cn.bugstack.domain.material.model.aggregate.MaterialAggregate;
import cn.bugstack.domain.material.repository.IMaterialRepository;
import cn.bugstack.domain.materialtype.model.aggregate.MaterialTypeAggregate;
import cn.bugstack.domain.materialtype.repository.IMaterialTypeRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class MaterialService implements IMaterialService {

    @Resource
    private IMaterialRepository materialRepository;
    @Resource
    private IMaterialTypeRepository materialTypeRepository;

    @Override
    public Long addNewMaterial(MaterialAggregate material) {
        if (material == null) {
            throw new IllegalArgumentException("原料信息不能为空");
        }
        validateMaterialCode(material.getMaterialCode(), null);
        validateName(material.getName());
        validateTypeId(material.getTypeId());
        validateUnit(material.getUnit());
        validateStatus(material.getStatus());
        validateTypeEnabled(material.getTypeId());
        return materialRepository.save(material);
    }

    @Override
    public void deleteMaterialById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("原料id不能为空");
        }
        MaterialAggregate current = materialRepository.queryById(id);
        if (current == null) {
            throw new IllegalArgumentException("原料不存在");
        }
        materialRepository.deleteById(id);
    }

    @Override
    public MaterialAggregate queryMaterialById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("原料id不能为空");
        }
        return materialRepository.queryById(id);
    }

    @Override
    public void updateMaterialById(MaterialAggregate material) {
        if (material == null) {
            throw new IllegalArgumentException("原料信息不能为空");
        }
        if (material.getId() == null) {
            throw new IllegalArgumentException("原料id不能为空");
        }

        MaterialAggregate current = materialRepository.queryById(material.getId());
        if (current == null) {
            throw new IllegalArgumentException("原料不存在");
        }

        validateMaterialCode(material.getMaterialCode(), material.getId());
        validateName(material.getName());
        validateTypeId(material.getTypeId());
        validateUnit(material.getUnit());
        validateStatus(material.getStatus());
        validateTypeEnabled(material.getTypeId());

        MaterialAggregate updated = MaterialAggregate.builder()
                .id(current.getId())
                .materialCode(material.getMaterialCode())
                .name(material.getName())
                .typeId(material.getTypeId())
                .unit(material.getUnit())
                .description(material.getDescription())
                .status(material.getStatus())
                .isDel(current.getIsDel())
                .createTime(current.getCreateTime())
                .updateTime(LocalDateTime.now())
                .build();
        materialRepository.updateById(updated);
    }

    private void validateMaterialCode(String materialCode, Long currentId) {
        if (materialCode == null || materialCode.trim().isEmpty()) {
            throw new IllegalArgumentException("原料编码不能为空");
        }
        MaterialAggregate existed = materialRepository.queryByMaterialCode(materialCode);
        if (existed == null) {
            return;
        }
        if (currentId == null || !currentId.equals(existed.getId())) {
            throw new IllegalArgumentException("原料编码已存在");
        }
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("原料名称不能为空");
        }
    }

    private void validateTypeId(Long typeId) {
        if (typeId == null) {
            throw new IllegalArgumentException("原料类型id不能为空");
        }
    }

    private void validateUnit(String unit) {
        if (unit == null || unit.trim().isEmpty()) {
            throw new IllegalArgumentException("计量单位不能为空");
        }
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("原料状态值非法");
        }
    }

    private void validateTypeEnabled(Long typeId) {
        MaterialTypeAggregate materialType = materialTypeRepository.queryById(typeId);
        if (materialType == null) {
            throw new IllegalArgumentException("原料类型不存在");
        }
        if (materialType.getStatus() == null || materialType.getStatus() != 1) {
            throw new IllegalArgumentException("原料类型未启用，不能使用");
        }
    }
}
