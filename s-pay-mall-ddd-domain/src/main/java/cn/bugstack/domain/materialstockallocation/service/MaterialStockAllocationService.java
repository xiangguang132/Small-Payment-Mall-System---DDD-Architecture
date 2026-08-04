package cn.bugstack.domain.materialstockallocation.service;

import cn.bugstack.domain.material.service.IMaterialService;
import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;
import cn.bugstack.domain.materialstock.repository.IMaterialStockRepository;
import cn.bugstack.domain.materialstockallocation.model.aggregate.MaterialStockAllocationAggregate;
import cn.bugstack.domain.materialstockallocation.model.vo.MaterialStockAllocationItemVO;
import cn.bugstack.domain.materialstockallocation.repository.IMaterialStockAllocationRepository;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MaterialStockAllocationService implements IMaterialStockAllocationService {

    @Resource
    private IMaterialService materialService;
    @Resource
    private IMaterialStockRepository materialStockRepository;
    @Resource
    private IMaterialStockAllocationRepository materialStockAllocationRepository;

    @Override
    public String create(Long materialId, Integer quantity, String reason) {
        validateCreateParams(materialId, quantity);
        materialService.validateMaterialEnabled(materialId);

        // 1. 按 materialId 查询所有可用库位库存
        List<MaterialStockAggregate> candidateStocks = materialStockRepository.queryAvailableByMaterialId(materialId);

        // 2. 按可用数量拆分生成 allocation item
        BigDecimal requestQty = BigDecimal.valueOf(quantity);
        List<MaterialStockAllocationItemVO> allocationItems = splitAllocationItems(candidateStocks, requestQty);

        // 3. 写入 material_stock_allocation 主单
        String allocationNo = generateAllocationNo();

        // 4. 写入 material_stock_allocation_item 明细
        materialStockAllocationRepository.create(
                allocationNo,
                materialId,
                null,
                requestQty,
                reason,
                allocationItems
        );

        // 5. 返回 allocationNo
        return allocationNo;
    }

    @Override
    public MaterialStockAllocationAggregate queryByAllocationNo(String allocationNo) {
        if (allocationNo == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料库存分配单号不能为空");
        }
        MaterialStockAllocationAggregate aggregate = materialStockAllocationRepository.queryByAllocationNo(allocationNo);
        if (aggregate == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "原料库存分配单不存在");
        }
        return aggregate;
    }

    @Override
    public MaterialStockAllocationAggregate queryById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料库存分配单ID不能为空");
        }
        MaterialStockAllocationAggregate aggregate = materialStockAllocationRepository.queryById(id);
        if (aggregate == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "原料库存分配单不存在");
        }
        return aggregate;
    }

    /**
     * 锁库
     * 直接锁定 material_stock 表，更新material_stock_allocation 和 material_allocation_item 的状态
     * @param allocationNo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lock(String allocationNo) {
        MaterialStockAllocationAggregate aggregate = queryWaitLockAllocation(allocationNo);

        List<MaterialStockAllocationItemVO> lockedItems = new ArrayList<>();
        BigDecimal totalLockedQty = lockAllocationItems(aggregate.getItems(), lockedItems);

        // 将数据设定到新创建的 数据对象里，方便一次性直接快速更新
        // 锁定原料分配表，更新状态为 1
        aggregate.setLockedQty(totalLockedQty);
        aggregate.setStatus(1);
        aggregate.setRetryCount(0);
        aggregate.setFailReason(null);
        aggregate.setUpdateTime(LocalDateTime.now());

        // 更新原料库存分配表 -》 持久化
        materialStockAllocationRepository.updateLockResult(aggregate, 0);
    }

    /**
     * 锁库失败时，自动释放本次已经锁定成功的原料。
     * @param allocationNo
     */
    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = AppException.class)
    public void lockWithAutoReleaseOnFailure(String allocationNo) {
        MaterialStockAllocationAggregate aggregate = queryWaitLockAllocation(allocationNo);

        List<MaterialStockAllocationItemVO> lockedItems = new ArrayList<>();
        try {
            BigDecimal totalLockedQty = lockAllocationItems(aggregate.getItems(), lockedItems);

            aggregate.setLockedQty(totalLockedQty);
            aggregate.setStatus(1);
            aggregate.setRetryCount(0);
            aggregate.setFailReason(null);
            aggregate.setUpdateTime(LocalDateTime.now());

            materialStockAllocationRepository.updateLockResult(aggregate, 0);
        } catch (Exception e) {
            releaseLockedItemsAfterLockFailure(aggregate, lockedItems, e);
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, e.getMessage(), e);
        }
    }

    private MaterialStockAllocationAggregate queryWaitLockAllocation(String allocationNo) {
        if (allocationNo == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "分配订单号不存在，无法锁定");
        }
        MaterialStockAllocationAggregate aggregate = materialStockAllocationRepository.queryByAllocationNo(allocationNo);
        if (aggregate == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "分配订单为空，无法锁定");
        }
        Integer status = aggregate.getStatus();
        if (status == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "分配单状态为空，无法锁定");
        }
        if (status == 1 || status == 2) {
            return null; // 或者在外层直接 return
        }
        if (status != 0) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "分配单不是待锁定状态");
        }
        if (aggregate.getItems() == null || aggregate.getItems().isEmpty()) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "分配单明细为空，无法锁定");
        }
        return aggregate;
    }

    private BigDecimal lockAllocationItems(List<MaterialStockAllocationItemVO> items,
                                           List<MaterialStockAllocationItemVO> lockedItems) {
        BigDecimal totalLockedQty = BigDecimal.ZERO;
        LocalDateTime now = LocalDateTime.now();

        for (MaterialStockAllocationItemVO item : items) {
            BigDecimal lockQty = valueOf(item.getAllocateQty());
            if (lockQty.compareTo(BigDecimal.ZERO) <= 0) {
                throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "分配明细锁定数量必须大于0");
            }
            if (!materialStockRepository.lockStock(item.getStockId(), lockQty)) {
                throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料可用库存不足，不能锁定");
            }

            // 锁定分配明细表，更新状态为 1
            item.setLockedQty(lockQty);
            item.setStatus(1);
            item.setUpdateTime(now);

            lockedItems.add(item);
            totalLockedQty = totalLockedQty.add(lockQty);
        }

        return totalLockedQty;
    }

    /**
     * 放库
     * @param allocationNo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void release(String allocationNo) {
        if (allocationNo == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "分配订单号不存在，无法释放");
        }
        MaterialStockAllocationAggregate aggregate = materialStockAllocationRepository.queryByAllocationNo(allocationNo);
        if (aggregate == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "分配订单为空，无法释放");
        }
        if (aggregate.getStatus() == null || aggregate.getStatus() != 1) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "分配单不是锁定状态");
        }
        if (aggregate.getItems() == null || aggregate.getItems().isEmpty()) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "分配单明细为空，无法释放");
        }

        BigDecimal totalReleasedQty = releaseLockedItems(aggregate.getItems());
        if (totalReleasedQty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "分配单没有可释放的锁定库存");
        }

        aggregate.setLockedQty(BigDecimal.ZERO);
        aggregate.setReleasedQty(totalReleasedQty);
        aggregate.setStatus(3);
        aggregate.setUpdateTime(LocalDateTime.now());

        materialStockAllocationRepository.updateLockResult(aggregate, 1);
    }

    private void releaseLockedItemsAfterLockFailure(MaterialStockAllocationAggregate aggregate,
                                                    List<MaterialStockAllocationItemVO> lockedItems,
                                                    Exception lockException) {
        if (lockedItems == null || lockedItems.isEmpty()) {
            return;
        }

        BigDecimal totalReleasedQty = releaseLockedItems(lockedItems);

        aggregate.setLockedQty(BigDecimal.ZERO);
        aggregate.setReleasedQty(valueOf(aggregate.getReleasedQty()).add(totalReleasedQty));
        aggregate.setStatus(3);
        aggregate.setFailReason("锁库失败，已自动释放：" + (lockException == null ? null : lockException.getMessage()));
        aggregate.setUpdateTime(LocalDateTime.now());

        materialStockAllocationRepository.updateLockResult(aggregate, 0);
    }

    private BigDecimal releaseLockedItems(List<MaterialStockAllocationItemVO> items) {
        BigDecimal totalReleasedQty = BigDecimal.ZERO;
        LocalDateTime now = LocalDateTime.now();

        for (MaterialStockAllocationItemVO item : items) {
            BigDecimal releaseQty = valueOf(item.getLockedQty());
            if (releaseQty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            if (!materialStockRepository.releaseStock(item.getStockId(), releaseQty)) {
                throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料锁定库存不足，不能释放");
            }

            item.setLockedQty(BigDecimal.ZERO);
            item.setReleasedQty(valueOf(item.getReleasedQty()).add(releaseQty));
            item.setStatus(3);
            item.setUpdateTime(now);

            totalReleasedQty = totalReleasedQty.add(releaseQty);
        }

        return totalReleasedQty;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoOutbound(String allocationNo) {
        if (allocationNo == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "分配订单号不存在，无法出库");
        }
        MaterialStockAllocationAggregate aggregate = materialStockAllocationRepository.queryByAllocationNo(allocationNo);
        if (aggregate == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "分配订单为空，无法出库");
        }
        Integer status = aggregate.getStatus();
        if (status == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "分配单状态为空，无法出库");
        }
        if (status == 2) {
            return;
        }
        if (status != 1) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "分配单不是锁定状态");
        }
        if (aggregate.getItems() == null || aggregate.getItems().isEmpty()) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "分配单明细为空，无法出库");
        }

        BigDecimal totalOutboundQty = BigDecimal.ZERO;
        for (MaterialStockAllocationItemVO item : aggregate.getItems()) {
            BigDecimal outboundQty = valueOf(item.getLockedQty());
            if (outboundQty.compareTo(BigDecimal.ZERO) <= 0) {
                throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "分配明细出库数量必须大于0");
            }
            if (!materialStockRepository.outboundLockedStock(item.getStockId(), outboundQty)) {
                throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料锁定库存不足，不能出库");
            }

            item.setLockedQty(BigDecimal.ZERO);
            item.setOutboundQty(outboundQty);
            item.setStatus(2);
            item.setUpdateTime(LocalDateTime.now());

            totalOutboundQty = totalOutboundQty.add(outboundQty);
        }

        aggregate.setLockedQty(BigDecimal.ZERO);
        aggregate.setOutboundQty(totalOutboundQty);
        aggregate.setStatus(2);
        aggregate.setUpdateTime(LocalDateTime.now());

        materialStockAllocationRepository.updateLockResult(aggregate, 1);
    }

    @Override
    public List<MaterialStockAllocationAggregate> queryByStatus(Integer status, Integer pageNo, Integer pageSize) {
        if (status == null || status < 0 || status > 5) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "分配单状态值非法");
        }
        if (pageNo == null || pageNo <= 0) {
            pageNo = 1;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 20;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }

        int offset = (pageNo - 1) * pageSize;
        return materialStockAllocationRepository.queryByStatus(status, offset, pageSize);
    }

    @Override
    public void recordLockFailure(String allocationNo, String failReason, Integer maxRetryCount) {
        if (allocationNo == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "分配订单号不存在，无法记录锁库失败");
        }
        if (maxRetryCount == null || maxRetryCount <= 0) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "最大重试次数必须大于0");
        }
        materialStockAllocationRepository.recordLockFailure(allocationNo, failReason, maxRetryCount);
    }

    private void validateCreateParams(Long materialId, Integer quantity) {
        if (materialId == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "物料ID不能为空");
        }
        if (quantity == null || quantity <= 0) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "分配数量必须大于0");
        }
    }

    // 拆分逻辑
    private List<MaterialStockAllocationItemVO> splitAllocationItems(List<MaterialStockAggregate> candidateStocks,
                                                                     BigDecimal requestQty) {
        // 判空
        // 如果库存序列为空，说明没有相关可用库存，那就生成不了任何的出库单
        if (candidateStocks == null || candidateStocks.isEmpty()) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "原料可用库存不足，不能创建分配单");
        }

        // 创建一个 详情vo 数组保存数据
        List<MaterialStockAllocationItemVO> allocationItems = new ArrayList<>();
        // 定义一个“剩余需求量”，初始等于总需求
        BigDecimal remainingQty = requestQty;
        // 排序号，用于标记第几笔明细
        int sortNo = 1;

        for (MaterialStockAggregate stock : candidateStocks) {
            // 获取当前遍历到的库位的可用数量
            BigDecimal availableQty = valueOf(stock.getAvailableQty());
            // 如果没有货存直接跳过
            if (availableQty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            // 【核心】 计算可用量
            // 在初始情况下，remainQty = RemainQty
            // allocateQty 的值 = availableQty 和 remainingQty 最小的
            BigDecimal allocateQty = availableQty.min(remainingQty);
            // 这个时候是使用完了该对象的，直接构建明细对象并加入列表
            // 我一共需要使用 100 个
            // 也就是说，情况1： 我 A对象 只有40个，使用掉之后将 A 和使用数量构建并添加到明细表，然后使用下一个的库存量去补充，不够的话继续找，如此往复
            // 情况2：B 对象 有 120 个，前面的 A 对象使用了 40 个，还需要 60 个，allocateQty 会变成 60，因此 B 会被使用 60 个
            allocationItems.add(MaterialStockAllocationItemVO.builder()
                    .stockId(stock.getId())
                    .materialId(stock.getMaterialId())
                    .storageAddress(stock.getStorageAddress())
                    .allocateQty(allocateQty)
                    .sortNo(sortNo++)
                    .build());

            // 使用 remainingQty 减去 allocateQty
            // 即 使用需求量 减去 已分配需求量 就是还需要的 需求量
            remainingQty = remainingQty.subtract(allocateQty);
            if (remainingQty.compareTo(BigDecimal.ZERO) == 0) {
                break;
            }
        }

        // 如果检查了所有的序列表之后发现还是不够，那就返回库存不足
        if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "可用库存不足，不能创建分配单");
        }

        return allocationItems;
    }

    private BigDecimal valueOf(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String generateAllocationNo() {
        return "MSA" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                ThreadLocalRandom.current().nextInt(1000, 10000);
    }
}
