package cn.bugstack.domain.warehouse.service;

import cn.bugstack.domain.warehouse.model.aggregate.WarehouseAggregate;
import cn.bugstack.domain.warehouse.repository.IWarehouseRepository;
import cn.bugstack.types.exception.AppException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WarehouseServiceTest {

    @Mock
    private IWarehouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseService warehouseService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldAddWarehouse() {
        WarehouseAggregate warehouse = warehouse("W001", "main", 0, 1);
        when(warehouseRepository.save(warehouse)).thenReturn(1L);

        assertEquals(Long.valueOf(1L), warehouseService.addWarehouse(warehouse));
    }

    @Test(expected = AppException.class)
    public void shouldRejectInvalidWarehouseType() {
        warehouseService.addWarehouse(warehouse("W001", "main", 9, 1));
    }

    @Test(expected = AppException.class)
    public void shouldRejectQueryWhenWarehouseMissing() {
        when(warehouseRepository.queryById(1L)).thenReturn(null);

        warehouseService.queryWarehouseById(1L);
    }

    @Test
    public void shouldUpdateWarehouse() {
        WarehouseAggregate warehouse = warehouse("W002", "second", 1, 0);
        warehouse.setId(1L);
        when(warehouseRepository.queryById(1L)).thenReturn(warehouse);

        warehouseService.updateWarehouseById(warehouse);

        verify(warehouseRepository).updateById(warehouse);
    }

    private WarehouseAggregate warehouse(String code, String name, Integer type, Integer status) {
        return WarehouseAggregate.builder()
                .warehouseCode(code)
                .name(name)
                .type(type)
                .status(status)
                .build();
    }
}
