package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.materialstockallocation.model.vo.MaterialStockAllocationItemVO;
import cn.bugstack.domain.materialstockallocation.repository.IMaterialStockAllocationRepository;
import cn.bugstack.infrastructure.dao.IMaterialStockAllocationDao;
import cn.bugstack.infrastructure.dao.IMaterialStockAllocationItemDao;
import cn.bugstack.infrastructure.dao.po.MaterialStockAllocation;
import cn.bugstack.infrastructure.dao.po.MaterialStockAllocationItem;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class MaterialStockAllocationRepository implements IMaterialStockAllocationRepository {

    private static final int STATUS_CREATED = 0;

    @Resource
    private IMaterialStockAllocationDao materialStockAllocationDao;
    @Resource
    private IMaterialStockAllocationItemDao materialStockAllocationItemDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(String allocationNo,
                       Long materialId,
                       Long requestStockId,
                       BigDecimal requestQty,
                       String reason,
                       List<MaterialStockAllocationItemVO> items) {
        LocalDateTime now = LocalDateTime.now();

        MaterialStockAllocation allocation = new MaterialStockAllocation();
        allocation.setAllocationNo(allocationNo);
        allocation.setMaterialId(materialId);
        allocation.setRequestStockId(requestStockId);
        allocation.setRequestQty(requestQty);
        allocation.setLockedQty(BigDecimal.ZERO);
        allocation.setOutboundQty(BigDecimal.ZERO);
        allocation.setReleasedQty(BigDecimal.ZERO);
        allocation.setStatus(STATUS_CREATED);
        allocation.setReason(reason);
        allocation.setIsDel(0);
        allocation.setCreateTime(now);
        allocation.setUpdateTime(now);
        materialStockAllocationDao.insert(allocation);

        for (MaterialStockAllocationItemVO itemVO : items) {
            MaterialStockAllocationItem item = new MaterialStockAllocationItem();
            item.setAllocationId(allocation.getId());
            item.setStockId(itemVO.getStockId());
            item.setMaterialId(itemVO.getMaterialId());
            item.setStorageAddress(itemVO.getStorageAddress());
            item.setAllocateQty(itemVO.getAllocateQty());
            item.setLockedQty(BigDecimal.ZERO);
            item.setOutboundQty(BigDecimal.ZERO);
            item.setReleasedQty(BigDecimal.ZERO);
            item.setSortNo(itemVO.getSortNo());
            item.setStatus(STATUS_CREATED);
            item.setIsDel(0);
            item.setCreateTime(now);
            item.setUpdateTime(now);
            materialStockAllocationItemDao.insert(item);
        }
    }
}
