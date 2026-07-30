package cn.bugstack.trigger.assembler;

import cn.bugstack.api.request.material.MaterialAddRequest;
import cn.bugstack.api.request.material.MaterialUpdateRequest;
import cn.bugstack.api.response.material.MaterialDetailResponse;
import cn.bugstack.domain.material.model.aggregate.MaterialAggregate;

public class MaterialAssembler {

    private MaterialAssembler() {
    }

    public static MaterialAggregate toAggregate(MaterialAddRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("原料信息不能为空");
        }
        return MaterialAggregate.create(
                trim(request.getMaterialCode()),
                trim(request.getName()),
                request.getTypeId(),
                trim(request.getUnit()),
                trim(request.getDescription()),
                request.getStatus()
        );
    }

    public static MaterialAggregate toUpdatedAggregate(MaterialAggregate current, MaterialUpdateRequest request) {
        if (current == null) {
            throw new IllegalArgumentException("原料不存在");
        }
        if (request == null) {
            throw new IllegalArgumentException("原料信息不能为空");
        }
        return MaterialAggregate.builder()
                .id(current.getId())
                .materialCode(request.getMaterialCode() != null ? trim(request.getMaterialCode()) : current.getMaterialCode())
                .name(request.getName() != null ? trim(request.getName()) : current.getName())
                .typeId(request.getTypeId() != null ? request.getTypeId() : current.getTypeId())
                .unit(request.getUnit() != null ? trim(request.getUnit()) : current.getUnit())
                .description(request.getDescription() != null ? trim(request.getDescription()) : current.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : current.getStatus())
                .isDel(current.getIsDel())
                .createTime(current.getCreateTime())
                .updateTime(current.getUpdateTime())
                .build();
    }

    public static MaterialDetailResponse toDetailResponse(MaterialAggregate material) {
        if (material == null) {
            throw new IllegalArgumentException("暂无相关原料详情");
        }
        MaterialDetailResponse response = new MaterialDetailResponse();
        response.setId(material.getId());
        response.setMaterialCode(material.getMaterialCode());
        response.setName(material.getName());
        response.setTypeId(material.getTypeId());
        response.setTypeName(material.getTypeName());
        response.setTypeDescription(material.getTypeDescription());
        response.setTypeCode(material.getTypeCode());
        response.setUnit(material.getUnit());
        response.setDescription(material.getDescription());
        response.setStatus(material.getStatus());
        response.setIsDel(material.getIsDel());
        response.setCreateTime(material.getCreateTime());
        response.setUpdateTime(material.getUpdateTime());
        return response;
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
