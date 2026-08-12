package cn.bugstack.domain.materialstockallocation.service;

import cn.bugstack.domain.material.service.IMaterialService;
import cn.bugstack.domain.materialstock.model.aggregate.MaterialStockAggregate;
import cn.bugstack.domain.materialstock.repository.IMaterialStockRepository;
import cn.bugstack.domain.materialstockallocation.model.aggregate.MaterialStockAllocationAggregate;
import cn.bugstack.domain.materialstockallocation.model.vo.MaterialStockAllocationItemVO;
import cn.bugstack.domain.materialstockallocation.repository.IMaterialStockAllocationRepository;
import cn.bugstack.types.exception.AppException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MaterialStockAllocationServiceTest {

    @Mock
    private IMaterialService materialService;

    @Mock
    private IMaterialStockRepository materialStockRepository;

    @Mock
    private IMaterialStockAllocationRepository allocationRepository;

    @InjectMocks
    private MaterialStockAllocationService allocationService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldCreateAllocationBySplittingAcrossAvailableStocks() {
        MaterialStockAggregate first = stock(1L, 1L, "A-01", "40.00");
        MaterialStockAggregate second = stock(2L, 1L, "B-02", "100.00");
        when(materialStockRepository.queryAvailableByMaterialId(1L))
                .thenReturn(Arrays.asList(first, second));

        String allocationNo = allocationService.create(1L, 100, "生产备料");

        assertNotNull(allocationNo);
        ArgumentCaptor<List> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(allocationRepository).create(
                anyString(),
                eq(1L),
                eq(null),
                eq(new BigDecimal("100")),
                eq("生产备料"),
                itemsCaptor.capture()
        );
        List<MaterialStockAllocationItemVO> items = itemsCaptor.getValue();
        assertEquals(2, items.size());
        assertEquals(new BigDecimal("40.00"), items.get(0).getAllocateQty());
        assertEquals(new BigDecimal("60.00"), items.get(1).getAllocateQty());
    }

    @Test(expected = AppException.class)
    public void shouldRejectCreateWhenAvailableStockIsNotEnough() {
        when(materialStockRepository.queryAvailableByMaterialId(1L))
                .thenReturn(Collections.singletonList(stock(1L, 1L, "A-01", "40.00")));

        allocationService.create(1L, 100, "生产备料");
    }

    @Test
    public void shouldLockAllocationAndItems() {
        MaterialStockAllocationItemVO item = item(1L, new BigDecimal("5.00"));
        MaterialStockAllocationAggregate aggregate = aggregate(0, item);
        when(allocationRepository.queryByAllocationNo("MSA001")).thenReturn(aggregate);
        when(materialStockRepository.lockStock(1L, new BigDecimal("5.00"))).thenReturn(true);

        allocationService.lock("MSA001");

        assertEquals(Integer.valueOf(1), aggregate.getStatus());
        assertEquals(new BigDecimal("5.00"), aggregate.getLockedQty());
        assertEquals(Integer.valueOf(1), item.getStatus());
        verify(allocationRepository).updateLockResult(same(aggregate), eq(0));
    }

    @Test
    public void shouldAutoReleaseAlreadyLockedItemsWhenLockFails() {
        MaterialStockAllocationItemVO first = item(1L, new BigDecimal("5.00"));
        MaterialStockAllocationItemVO second = item(2L, new BigDecimal("5.00"));
        MaterialStockAllocationAggregate aggregate = aggregate(0, first, second);
        when(allocationRepository.queryByAllocationNo("MSA001")).thenReturn(aggregate);
        when(materialStockRepository.lockStock(1L, new BigDecimal("5.00"))).thenReturn(true);
        when(materialStockRepository.lockStock(2L, new BigDecimal("5.00"))).thenReturn(false);
        when(materialStockRepository.releaseStock(1L, new BigDecimal("5.00"))).thenReturn(true);

        try {
            allocationService.lockWithAutoReleaseOnFailure("MSA001");
        } catch (AppException expected) {
            assertTrue(expected.getInfo().contains("库存不足"));
        }

        assertEquals(Integer.valueOf(3), aggregate.getStatus());
        assertEquals(BigDecimal.ZERO, aggregate.getLockedQty());
        assertTrue(aggregate.getFailReason().contains("已自动释放"));
        verify(allocationRepository).updateLockResult(same(aggregate), eq(0));
    }

    @Test
    public void shouldAutoOutboundLockedAllocation() {
        MaterialStockAllocationItemVO item = item(1L, new BigDecimal("5.00"));
        item.setLockedQty(new BigDecimal("5.00"));
        MaterialStockAllocationAggregate aggregate = aggregate(1, item);
        when(allocationRepository.queryByAllocationNo("MSA001")).thenReturn(aggregate);
        when(materialStockRepository.outboundLockedStock(1L, new BigDecimal("5.00"))).thenReturn(true);

        allocationService.autoOutbound("MSA001");

        assertEquals(Integer.valueOf(2), aggregate.getStatus());
        assertEquals(new BigDecimal("5.00"), aggregate.getOutboundQty());
        assertEquals(new BigDecimal("5.00"), item.getOutboundQty());
        verify(allocationRepository).updateLockResult(same(aggregate), eq(1));
    }

    @Test
    public void shouldApplyDefaultPagination() {
        allocationService.queryByStatus(0, null, null);

        verify(allocationRepository).queryByStatus(0, 0, 20);
    }

    private MaterialStockAggregate stock(Long id, Long materialId, String address, String availableQty) {
        return MaterialStockAggregate.builder()
                .id(id)
                .materialId(materialId)
                .storageAddress(address)
                .availableQty(new BigDecimal(availableQty))
                .build();
    }

    private MaterialStockAllocationItemVO item(Long stockId, BigDecimal allocateQty) {
        return MaterialStockAllocationItemVO.builder()
                .stockId(stockId)
                .materialId(1L)
                .allocateQty(allocateQty)
                .build();
    }

    private MaterialStockAllocationAggregate aggregate(Integer status, MaterialStockAllocationItemVO... items) {
        return MaterialStockAllocationAggregate.builder()
                .allocationNo("MSA001")
                .materialId(1L)
                .requestQty(new BigDecimal("10.00"))
                .status(status)
                .items(Arrays.asList(items))
                .build();
    }
}
