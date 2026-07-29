package cn.bugstack.trigger.assembler;

import cn.bugstack.api.request.producttype.ProductTypeAddRequest;
import cn.bugstack.domain.producttype.model.aggregate.ProductTypeAggregate;

public class ProductTypeAssembler {

    private ProductTypeAssembler() {
    }

    public static ProductTypeAggregate toAggregate(ProductTypeAddRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("品类信息不能为空");
        }
        return ProductTypeAggregate.create(
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
