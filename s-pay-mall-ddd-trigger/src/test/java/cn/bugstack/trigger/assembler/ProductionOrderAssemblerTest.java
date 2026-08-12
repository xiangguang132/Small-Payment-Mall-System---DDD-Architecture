package cn.bugstack.trigger.assembler;

import cn.bugstack.api.response.production.ProductionOrderDetailResponse;
import cn.bugstack.domain.production.model.aggregate.ProductionOrderAggregate;
import cn.bugstack.domain.production.model.vo.ProductionOrderMaterialVO;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class ProductionOrderAssemblerTest {

    @Test
    public void shouldMapAggregateToDetailResponse() {
        ProductionOrderMaterialVO material = ProductionOrderMaterialVO.builder()
                .id(1L)
                .productionOrderId(10L)
                .materialId(2L)
                .materialQuantity(3)
                .allocationNo("MSA001")
                .status(1)
                .build();
        ProductionOrderAggregate order = ProductionOrderAggregate.builder()
                .id(10L)
                .orderNo("PO001")
                .requestNo("REQ-001")
                .productId(2L)
                .productQuantity(3L)
                .warehouseId(4L)
                .status(1)
                .materials(Collections.singletonList(material))
                .build();

        ProductionOrderDetailResponse response = ProductionOrderAssembler.toDetailResponse(order);

        assertEquals("PO001", response.getOrderNo());
        assertEquals("REQ-001", response.getRequestNo());
        assertEquals(1, response.getMaterials().size());
        ProductionOrderDetailResponse.MaterialItem item = response.getMaterials().get(0);
        assertEquals(Long.valueOf(2L), item.getMaterialId());
        assertEquals(Integer.valueOf(3), item.getMaterialQuantity());
        assertEquals("MSA001", item.getAllocationNo());
    }
}
