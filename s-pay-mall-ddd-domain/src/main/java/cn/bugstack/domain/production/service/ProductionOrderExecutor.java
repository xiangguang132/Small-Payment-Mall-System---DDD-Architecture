package cn.bugstack.domain.production.service;
import cn.bugstack.domain.materialstockallocation.service.IMaterialStockAllocationService;
import cn.bugstack.domain.production.model.aggregate.ProductionOrderAggregate;
import cn.bugstack.domain.production.model.vo.ProductionOrderMaterialVO;
import cn.bugstack.domain.production.repository.IProductionOrderRepository;
import cn.bugstack.domain.warehousestock.service.IStockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class ProductionOrderExecutor {

    @Resource
    private IProductionOrderRepository productionOrderRepository;

    @Resource
    private IMaterialStockAllocationService materialStockAllocationService;

    @Resource
    private IStockService stockService;

    @Transactional(rollbackFor = Exception.class)
    public void execute(Long productionOrderId) {
        ProductionOrderAggregate order = productionOrderRepository.queryById(productionOrderId);
        if (order == null) {
            throw new IllegalArgumentException("生产需求单不存在");
        }
        if (order.getStatus() == null || order.getStatus() != 0) {
            return ;
        }
        if (order.getMaterials() == null || order.getMaterials().isEmpty()) {
            throw new IllegalArgumentException("生产需求但没有原料明细");
        }
        // 如果没有以上异常情况，就修改状态为 1
        // 生产需求单状态激活（接单）
        productionOrderRepository.updateOrderStatus(order.getId(), 1);
        // 针对生产需求单需要的 每一种 “原料” ，都进行 分配-锁定-扣减
        for (ProductionOrderMaterialVO material : order.getMaterials()) {
            // 分配
            // 该方法 -》 生成一份原料库存分配单，并返回分配单单号
            String allocationNo = materialStockAllocationService.create(
                    material.getMaterialId(),
                    material.getMaterialQuantity(),
                    "生产单" + order.getOrderNo() + "备料"
            );
            // 锁定 -》 锁定原料库存
            materialStockAllocationService.lock(allocationNo);
            // 更新数据库状态为 1 -》 被锁
            productionOrderRepository.updateMaterialAllocationNo(material.getId(), allocationNo, 1);
            // 自动出库
            materialStockAllocationService.autoOutbound(allocationNo);

            productionOrderRepository.updateMaterialAllocationNo(material.getId(), allocationNo, 2);
        }

        // 产品入库
        stockService.inbound(
                order.getWarehouseId(),
                order.getProductId(),
                order.getProductQuantity().intValue()
        );

        productionOrderRepository.updateOrderStatus(order.getId(), 2);
    }

}
