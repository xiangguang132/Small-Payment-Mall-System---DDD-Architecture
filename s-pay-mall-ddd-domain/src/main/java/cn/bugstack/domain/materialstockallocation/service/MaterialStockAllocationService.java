package cn.bugstack.domain.materialstockallocation.service;

import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;
import cn.bugstack.domain.materialstock.repository.IMaterialStockRepository;
import cn.bugstack.domain.materialstockallocation.model.aggregate.MaterialStockAllocationAggregate;
import cn.bugstack.domain.materialstockallocation.model.vo.MaterialStockAllocationItemVO;
import cn.bugstack.domain.materialstockallocation.repository.IMaterialStockAllocationRepository;
import org.springframework.stereotype.Service;

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
    private IMaterialStockRepository materialStockRepository;
    @Resource
    private IMaterialStockAllocationRepository materialStockAllocationRepository;

    @Override
    public String create(Long requestStockId, Integer quantity, String reason) {
        validateCreateParams(requestStockId, quantity);

        // 1. 查询 requestStockId 对应原料库存 ---》 查询的是 materialStock 的
        MaterialStockAggregate requestStock = materialStockRepository.queryById(requestStockId);
        if (requestStock == null) {
            throw new IllegalArgumentException("请求原料仓储不存在");
        }

        Long materialId = requestStock.getMaterialId();
        // 2. 按 materialId 查询其他可用库位库存 ---》 查询的是 materialStock 的
        // 但是需要排除自己
        List<MaterialStockAggregate> candidateStocks =
                materialStockRepository.queryAvailableByMaterialIdExcludeStockId(
                        materialId,
                        requestStock.getId()
                );

        // 3. 按可用数量拆分生成 allocation item
        BigDecimal requestQty = BigDecimal.valueOf(quantity);
        List<MaterialStockAllocationItemVO> allocationItems = splitAllocationItems(candidateStocks, requestQty);

        // 4. 写入 material_stock_allocation 主单
        String allocationNo = generateAllocationNo();

        // 5. 写入 material_stock_allocation_item 明细
        materialStockAllocationRepository.create(
                allocationNo,
                materialId,
                requestStockId,
                requestQty,
                reason,
                allocationItems
        );

        // 6. 返回 allocationNo
        return allocationNo;
    }

    @Override
    public MaterialStockAllocationAggregate queryByAllocationNo(String allocationNo) {
        if (allocationNo == null) {
            throw new IllegalArgumentException("原料库存分配单号不能为空");
        }
        MaterialStockAllocationAggregate aggregate = materialStockAllocationRepository.queryByAllocationNo(allocationNo);
        if (aggregate == null) {
            throw new IllegalArgumentException("原料库存分配单不存在");
        }
        return aggregate;
    }

    @Override
    public MaterialStockAllocationAggregate queryById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("原料库存分配单ID不能为空");
        }
        MaterialStockAllocationAggregate aggregate = materialStockAllocationRepository.queryById(id);
        if (aggregate == null) {
            throw new IllegalArgumentException("原料库存分配单不存在");
        }
        return aggregate;
    }

    private void validateCreateParams(Long requestStockId, Integer quantity) {
        if (requestStockId == null) {
            throw new IllegalArgumentException("请求入口库存ID不能为空");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("分配数量必须大于0");
        }
    }

    // 拆分逻辑
    private List<MaterialStockAllocationItemVO> splitAllocationItems(List<MaterialStockAggregate> candidateStocks,
                                                                     BigDecimal requestQty) {
        // 判空
        // 如果库存序列为空，说明没有库存中没有相关数据，那就生成不了任何的出库单
        if (candidateStocks == null || candidateStocks.isEmpty()) {
            throw new IllegalArgumentException("无其他可用库位库存，不能创建分配单");
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
            throw new IllegalArgumentException("其他库位可用库存不足，不能创建分配单");
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
