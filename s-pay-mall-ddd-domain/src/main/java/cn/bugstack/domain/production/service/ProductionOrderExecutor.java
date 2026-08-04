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

/**
 * 生产订单执行器
 * 驱动一个生产订单从“接单”到“完成”的整个自动化流程
 */
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

            // 获取当前物料在数据库中已有的分配单单号（如果是首次执行，这里会是 null）
            String allocationNo = material.getAllocationNo();
            // 获取当前物料的状态。如果数据库中还没记录状态，默认初始化为 0（未分配）
            Integer materialStatus = material.getStatus() == null ? 0 : material.getStatus();

            // 核心检查点：如果分配单号为空，说明这个物料还没有经历过“分配”流程
            if (allocationNo == null || allocationNo.trim().isEmpty()) {
                // 分配
                // 该方法 -》 生成一份原料库存分配单，并返回分配单单号
                allocationNo = executeStage(ProductionExecuteStageVO.CREATE_ALLOCATION,
                        () -> materialStockAllocationService.create(
                                material.getMaterialId(),
                                material.getMaterialQuantity(),
                                "生产单" + order.getOrderNo() + "备料"
                        ));

                // 持久化到数据库
                // 锁定 -》 锁定原料库存
                final String createdAllocationNo = allocationNo;
                executeStage(ProductionExecuteStageVO.CREATE_ALLOCATION,
                        () -> productionOrderRepository.updateMaterialAllocationNo(
                                material.getId(), createdAllocationNo, 0));
            }

            final String currentAllocationNo = allocationNo;

            // 检查点：如果当前状态小于 1（即状态为0，未锁定），才执行锁定
            if (materialStatus < 1) {
                executeStage(ProductionExecuteStageVO.LOCK_MATERIAL,
                        () -> materialStockAllocationService.lockWithAutoReleaseOnFailure(currentAllocationNo));

                executeStage(ProductionExecuteStageVO.LOCK_MATERIAL,
                        () -> productionOrderRepository.updateMaterialAllocationNo(
                                material.getId(), currentAllocationNo, 1));

                materialStatus = 1;
            }

            // 检查点：如果当前状态小于 2（即状态为0或1，未出库），才执行出库
            if (materialStatus < 2) {
                executeStage(ProductionExecuteStageVO.OUTBOUND_MATERIAL,
                        () -> materialStockAllocationService.autoOutbound(currentAllocationNo));

                executeStage(ProductionExecuteStageVO.OUTBOUND_MATERIAL,
                        () -> productionOrderRepository.updateMaterialAllocationNo(
                                material.getId(), currentAllocationNo, 2));
            }
        }

        // 产品入库
        executeStage(ProductionExecuteStageVO.INBOUND_PRODUCT,
                () -> stockService.inbound(
                        order.getWarehouseId(),
                        order.getProductId(),
                        order.getProductQuantity().intValue(),
                        "PRODUCTION_ORDER",
                        order.getOrderNo()
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
