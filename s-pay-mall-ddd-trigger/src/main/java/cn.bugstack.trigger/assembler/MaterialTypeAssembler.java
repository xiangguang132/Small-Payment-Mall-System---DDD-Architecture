package cn.bugstack.trigger.assembler;

import cn.bugstack.api.request.materialtype.MaterialTypeAddRequest;
import cn.bugstack.domain.materialtype.model.aggregate.MaterialTypeAggregate;

public class MaterialTypeAssembler {

    private MaterialTypeAssembler() {
    }

    public static MaterialTypeAggregate toAggregate(MaterialTypeAddRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("原料分类信息不能为空");
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

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
