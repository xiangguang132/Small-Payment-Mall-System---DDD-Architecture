package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.materialstockallocation.model.aggregate.MaterialStockAllocationAggregate;
import cn.bugstack.domain.materialstockallocation.model.vo.MaterialStockAllocationItemVO;
import cn.bugstack.domain.materialstockallocation.repository.IMaterialStockAllocationRepository;
import cn.bugstack.infrastructure.dao.IMaterialStockAllocationDao;
import cn.bugstack.infrastructure.dao.IMaterialStockAllocationItemDao;
import cn.bugstack.infrastructure.dao.po.material.MaterialStockAllocation;
import cn.bugstack.infrastructure.dao.po.material.MaterialStockAllocationItem;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
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
        if (allocationNo == null || allocationNo.trim().isEmpty()) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料库存分配单号不能为空");
        }
        if (materialId == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料ID不能为空");
        }
        if (requestQty == null || requestQty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "分配数量必须大于0");
        }
        if (items == null || items.isEmpty()) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料库存分配单明细不能为空");
        }
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLockResult(MaterialStockAllocationAggregate aggregate, Integer expectedStatus) {
        if (aggregate == null || aggregate.getId() == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料库存分配单信息或ID不能为空");
        }
        if (expectedStatus == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料库存分配单期望状态不能为空");
        }

        if (materialStockAllocationDao.update(toAllocationPo(aggregate), expectedStatus) != 1) {
            throw new AppException(ResponseCode.CONFLICT, "原料库存分配单状态已变化，不能重复处理");
        }

        if (aggregate.getItems() == null || aggregate.getItems().isEmpty()) {
            return;
        }
        for (MaterialStockAllocationItemVO itemVO : aggregate.getItems()) {
            materialStockAllocationItemDao.update(toItemPo(itemVO));
        }
    }

    @Override
    public List<MaterialStockAllocationAggregate> queryByStatus(Integer status, int offset, Integer pageSize) {
        List<MaterialStockAllocation> allocations = materialStockAllocationDao.queryByStatus(status, offset, pageSize);

        if  (allocations == null) {
            return Collections.emptyList();
        }

        return allocations.stream()
                .map(this::toAggregateWithItems)
                .collect(Collectors.toList());
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
        allocation.setRetryCount(0);
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
                .retryCount(allocation.getRetryCount())
                .failReason(allocation.getFailReason())
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

    private MaterialStockAllocation toAllocationPo(MaterialStockAllocationAggregate aggregate) {
        MaterialStockAllocation allocation = new MaterialStockAllocation();
        allocation.setId(aggregate.getId());
        allocation.setAllocationNo(aggregate.getAllocationNo());
        allocation.setMaterialId(aggregate.getMaterialId());
        allocation.setRequestStockId(aggregate.getRequestStockId());
        allocation.setRequestQty(aggregate.getRequestQty());
        allocation.setLockedQty(aggregate.getLockedQty());
        allocation.setOutboundQty(aggregate.getOutboundQty());
        allocation.setReleasedQty(aggregate.getReleasedQty());
        allocation.setStatus(aggregate.getStatus());
        allocation.setRetryCount(aggregate.getRetryCount());
        allocation.setFailReason(aggregate.getFailReason());
        allocation.setReason(aggregate.getReason());
        allocation.setIsDel(aggregate.getIsDel());
        allocation.setCreateTime(aggregate.getCreateTime());
        allocation.setUpdateTime(aggregate.getUpdateTime());
        return allocation;
    }

    private MaterialStockAllocationItem toItemPo(MaterialStockAllocationItemVO itemVO) {
        MaterialStockAllocationItem item = new MaterialStockAllocationItem();
        item.setId(itemVO.getId());
        item.setAllocationId(itemVO.getAllocationId());
        item.setStockId(itemVO.getStockId());
        item.setMaterialId(itemVO.getMaterialId());
        item.setStorageAddress(itemVO.getStorageAddress());
        item.setAllocateQty(itemVO.getAllocateQty());
        item.setLockedQty(itemVO.getLockedQty());
        item.setOutboundQty(itemVO.getOutboundQty());
        item.setReleasedQty(itemVO.getReleasedQty());
        item.setSortNo(itemVO.getSortNo());
        item.setStatus(itemVO.getStatus());
        item.setIsDel(itemVO.getIsDel());
        item.setCreateTime(itemVO.getCreateTime());
        item.setUpdateTime(itemVO.getUpdateTime());
        return item;
    }

    @Override
    public void recordLockFailure(String allocationNo, String failReason, Integer maxRetryCount) {
        if (allocationNo == null || allocationNo.trim().isEmpty()) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料库存分配单号不能为空");
        }
        if (maxRetryCount == null || maxRetryCount <= 0) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "最大重试次数必须大于0");
        }
        materialStockAllocationDao.recordLockFailure(allocationNo, failReason, maxRetryCount);
    }
}
