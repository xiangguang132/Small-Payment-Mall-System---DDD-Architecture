package cn.bugstack.domain.materialstockallocation.repository;

import cn.bugstack.domain.materialstockallocation.model.aggregate.MaterialStockAllocationAggregate;
import cn.bugstack.domain.materialstockallocation.model.vo.MaterialStockAllocationItemVO;

import java.math.BigDecimal;
import java.util.List;

public interface IMaterialStockAllocationRepository {

    void create(String allocationNo,
                Long materialId,
                Long requestStockId,
                BigDecimal requestQty,
                String reason,
                List<MaterialStockAllocationItemVO> items);

    MaterialStockAllocationAggregate queryByAllocationNo(String allocationNo);

    MaterialStockAllocationAggregate queryById(Long id);

    void updateLockResult(MaterialStockAllocationAggregate aggregate);
}
