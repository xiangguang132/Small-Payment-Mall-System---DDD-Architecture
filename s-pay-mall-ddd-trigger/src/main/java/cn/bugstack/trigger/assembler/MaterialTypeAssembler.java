package cn.bugstack.trigger.assembler;

import cn.bugstack.api.request.materialtype.MaterialTypeAddRequest;
import cn.bugstack.api.request.materialtype.MaterialTypeUpdateRequest;
import cn.bugstack.api.response.materialtype.MaterialTypeDetailResponse;
import cn.bugstack.domain.materialtype.model.aggregate.MaterialTypeAggregate;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;

public class MaterialTypeAssembler {

    private MaterialTypeAssembler() {
    }

    public static MaterialTypeAggregate toAggregate(MaterialTypeAddRequest request) {
        if (request == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料分类信息不能为空");
        }
        return MaterialTypeAggregate.create(
                request.getParentId(),
                trim(request.getName()),
                trim(request.getDescription()),
                trim(request.getTypeCode()),
                request.getSort(),
                request.getStatus()
        );
    }

    public static MaterialTypeAggregate toUpdatedAggregate(MaterialTypeAggregate current, MaterialTypeUpdateRequest request) {
        if (current == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "原料分类不存在");
        }
        if (request == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料分类信息不能为空");
        }
        return MaterialTypeAggregate.builder()
                .id(current.getId())
                .parentId(request.getParentId() != null ? request.getParentId() : current.getParentId())
                .name(request.getName() != null ? trim(request.getName()) : current.getName())
                .description(request.getDescription() != null ? trim(request.getDescription()) : current.getDescription())
                .typeCode(request.getTypeCode() != null ? trim(request.getTypeCode()) : current.getTypeCode())
                .sort(request.getSort() != null ? request.getSort() : current.getSort())
                .status(request.getStatus() != null ? request.getStatus() : current.getStatus())
                .isDel(current.getIsDel())
                .createTime(current.getCreateTime())
                .updateTime(current.getUpdateTime())
                .build();
    }

    public static MaterialTypeDetailResponse toDetailResponse(MaterialTypeAggregate materialType) {
        if (materialType == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "暂无相关原料分类详情");
        }
        MaterialTypeDetailResponse response = new MaterialTypeDetailResponse();
        response.setId(materialType.getId());
        response.setParentId(materialType.getParentId());
        response.setName(materialType.getName());
        response.setDescription(materialType.getDescription());
        response.setTypeCode(materialType.getTypeCode());
        response.setSort(materialType.getSort());
        response.setStatus(materialType.getStatus());
        response.setIsDel(materialType.getIsDel());
        response.setCreateTime(materialType.getCreateTime());
        response.setUpdateTime(materialType.getUpdateTime());
        return response;
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
