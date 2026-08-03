package cn.bugstack.domain.production.service;
import cn.bugstack.domain.materialstockallocation.service.IMaterialStockAllocationService;
import cn.bugstack.domain.production.exception.ProductionExecuteException;
import cn.bugstack.domain.production.model.aggregate.ProductionOrderAggregate;
import cn.bugstack.domain.production.model.vo.ProductionExecuteStageVO;
import cn.bugstack.domain.production.model.vo.ProductionOrderMaterialVO;
import cn.bugstack.domain.production.model.vo.ProductionOrderStatusVO;
import cn.bugstack.domain.production.repository.IProductionOrderRepository;
import cn.bugstack.domain.warehousestock.service.IStockService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ProductionOrderExecutor {

    @Resource
    private IProductionOrderRepository productionOrderRepository;

    @Resource
    private IMaterialStockAllocationService materialStockAllocationService;

    @Resource
    private IStockService stockService;

    public void execute(Long productionOrderId) {
        ProductionOrderAggregate order = productionOrderRepository.queryById(productionOrderId);
        if (order == null) {
            throw new ProductionExecuteException(ProductionExecuteStageVO.QUERY_ORDER, "生产需求单不存在");
        }
        if (!ProductionOrderStatusVO.canExecute(order.getStatus())) {
            return ;
        }
        if (order.getMaterials() == null || order.getMaterials().isEmpty()) {
            throw new ProductionExecuteException(ProductionExecuteStageVO.QUERY_ORDER, "生产需求单没有原料明细");
        }
        // 如果没有以上异常情况，就修改状态为 1
        // 生产需求单状态激活（接单）
        executeStage(ProductionExecuteStageVO.ACCEPT_ORDER,
                () -> productionOrderRepository.updateOrderStatus(order.getId(), ProductionOrderStatusVO.PROCESSING));
        // 针对生产需求单需要的 每一种 “原料” ，都进行 分配-锁定-扣减
        for (ProductionOrderMaterialVO material : order.getMaterials()) {
            // 分配
            // 该方法 -》 生成一份原料库存分配单，并返回分配单单号
            String allocationNo = executeStage(ProductionExecuteStageVO.CREATE_ALLOCATION,
                    () -> materialStockAllocationService.create(
                            material.getMaterialId(),
                            material.getMaterialQuantity(),
                            "生产单" + order.getOrderNo() + "备料"
                    ));
            // 锁定 -》 锁定原料库存
            executeStage(ProductionExecuteStageVO.LOCK_MATERIAL,
                    () -> materialStockAllocationService.lockWithAutoReleaseOnFailure(allocationNo));
            // 更新数据库状态为 1 -》 被锁
            executeStage(ProductionExecuteStageVO.LOCK_MATERIAL,
                    () -> productionOrderRepository.updateMaterialAllocationNo(material.getId(), allocationNo, 1));
            // 自动出库
            executeStage(ProductionExecuteStageVO.OUTBOUND_MATERIAL,
                    () -> materialStockAllocationService.autoOutbound(allocationNo));

            executeStage(ProductionExecuteStageVO.OUTBOUND_MATERIAL,
                    () -> productionOrderRepository.updateMaterialAllocationNo(material.getId(), allocationNo, 2));
        }

        // 产品入库
        executeStage(ProductionExecuteStageVO.INBOUND_PRODUCT,
                () -> stockService.inbound(
                        order.getWarehouseId(),
                        order.getProductId(),
                        order.getProductQuantity().intValue()
                ));

        executeStage(ProductionExecuteStageVO.COMPLETE_ORDER,
                () -> productionOrderRepository.updateOrderStatus(order.getId(), ProductionOrderStatusVO.COMPLETED));
    }

    private void executeStage(ProductionExecuteStageVO stage, Runnable action) {
        try {
            action.run();
        } catch (ProductionExecuteException e) {
            throw e;
        } catch (Exception e) {
            throw new ProductionExecuteException(stage, e);
        }
    }

    private <T> T executeStage(ProductionExecuteStageVO stage, StageSupplier<T> action) {
        try {
            return action.get();
        } catch (ProductionExecuteException e) {
            throw e;
        } catch (Exception e) {
            throw new ProductionExecuteException(stage, e);
        }
    }

    private interface StageSupplier<T> {
        T get();
    }

}
