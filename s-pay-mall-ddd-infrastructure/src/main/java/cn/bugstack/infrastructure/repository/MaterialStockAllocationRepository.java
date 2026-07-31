package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.materialstockallocation.model.aggregate.MaterialStockAllocationAggregate;
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
import java.util.stream.Collectors;

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
        allocation.setReason(reason);
        initCreatedFields(allocation, now);
        materialStockAllocationDao.insert(allocation);

        for (MaterialStockAllocationItemVO itemVO : items) {
            MaterialStockAllocationItem item = buildCreateItem(allocation.getId(), itemVO, now);
            materialStockAllocationItemDao.insert(item);
        }
    }

    @Override
    public MaterialStockAllocationAggregate queryByAllocationNo(String allocationNo) {
        MaterialStockAllocation allocation = materialStockAllocationDao.queryByAllocationNo(allocationNo);
        if (allocation == null) {
            return null;
        }

        return toAggregateWithItems(allocation);
    }

    @Override
    public MaterialStockAllocationAggregate queryById(Long id) {
        MaterialStockAllocation allocation = materialStockAllocationDao.queryById(id);
        if (allocation == null) {
            return null;
        }

        return toAggregateWithItems(allocation);
    }

    private MaterialStockAllocationAggregate toAggregateWithItems(MaterialStockAllocation allocation) {
        List<MaterialStockAllocationItem> items = materialStockAllocationItemDao.queryByAllocationId(allocation.getId());
        return toAggregate(allocation, items);
    }

    private MaterialStockAllocationItem buildCreateItem(Long allocationId,
                                                        MaterialStockAllocationItemVO itemVO,
                                                        LocalDateTime now) {
        MaterialStockAllocationItem item = new MaterialStockAllocationItem();
        item.setAllocationId(allocationId);
        item.setStockId(itemVO.getStockId());
        item.setMaterialId(itemVO.getMaterialId());
        item.setStorageAddress(itemVO.getStorageAddress());
        item.setAllocateQty(itemVO.getAllocateQty());
        item.setSortNo(itemVO.getSortNo());
        initCreatedFields(item, now);
        return item;
    }

    private void initCreatedFields(MaterialStockAllocation allocation, LocalDateTime now) {
        allocation.setLockedQty(BigDecimal.ZERO);
        allocation.setOutboundQty(BigDecimal.ZERO);
        allocation.setReleasedQty(BigDecimal.ZERO);
        allocation.setStatus(STATUS_CREATED);
        allocation.setIsDel(0);
        allocation.setCreateTime(now);
        allocation.setUpdateTime(now);
    }

    private void initCreatedFields(MaterialStockAllocationItem item, LocalDateTime now) {
        item.setLockedQty(BigDecimal.ZERO);
        item.setOutboundQty(BigDecimal.ZERO);
        item.setReleasedQty(BigDecimal.ZERO);
        item.setStatus(STATUS_CREATED);
        item.setIsDel(0);
        item.setCreateTime(now);
        item.setUpdateTime(now);
    }

    private MaterialStockAllocationAggregate toAggregate(MaterialStockAllocation allocation,
                                                         List<MaterialStockAllocationItem> items) {
        return MaterialStockAllocationAggregate.builder()
                .id(allocation.getId())
                .allocationNo(allocation.getAllocationNo())
                .materialId(allocation.getMaterialId())
                .requestStockId(allocation.getRequestStockId())
                .requestQty(allocation.getRequestQty())
                .lockedQty(allocation.getLockedQty())
                .outboundQty(allocation.getOutboundQty())
                .releasedQty(allocation.getReleasedQty())
                .status(allocation.getStatus())
                .reason(allocation.getReason())
                .isDel(allocation.getIsDel())
                .createTime(allocation.getCreateTime())
                .updateTime(allocation.getUpdateTime())
                .items(toItemVOList(items))
                .build();
    }

    private List<MaterialStockAllocationItemVO> toItemVOList(List<MaterialStockAllocationItem> items) {
        if (items == null) {
            return null;
        }
        return items.stream()
                .map(this::toItemVO)
                .collect(Collectors.toList());
    }

    private MaterialStockAllocationItemVO toItemVO(MaterialStockAllocationItem item) {
        return MaterialStockAllocationItemVO.builder()
                .id(item.getId())
                .allocationId(item.getAllocationId())
                .stockId(item.getStockId())
                .materialId(item.getMaterialId())
                .storageAddress(item.getStorageAddress())
                .allocateQty(item.getAllocateQty())
                .lockedQty(item.getLockedQty())
                .outboundQty(item.getOutboundQty())
                .releasedQty(item.getReleasedQty())
                .sortNo(item.getSortNo())
                .status(item.getStatus())
                .isDel(item.getIsDel())
                .createTime(item.getCreateTime())
                .updateTime(item.getUpdateTime())
                .build();
    }
}
