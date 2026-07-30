package cn.bugstack.domain.materialtype.service;

import cn.bugstack.domain.materialtype.model.aggregate.MaterialTypeAggregate;
import cn.bugstack.domain.materialtype.model.vo.MaterialTypeStatusVO;
import cn.bugstack.domain.materialtype.repository.IMaterialTypeRepository;
import cn.bugstack.domain.producttype.model.vo.ProductTypeStatusVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MaterialTypeService implements IMaterialTypeService {

    @Resource
    private IMaterialTypeRepository materialTypeRepository;

    @Override
    public Long addNewMaterialType(MaterialTypeAggregate materialType) {
        if (materialType == null) {
            throw new IllegalArgumentException("物料分类不能为空");
        }
        validateStatus(materialType.getStatus());
        return materialTypeRepository.save(materialType);
    }

    private void validateStatus(Integer status) {
        if (!MaterialTypeStatusVO.isValid(status)) {
            throw new IllegalArgumentException("原料分类状态值非法");
        }
    }
}
