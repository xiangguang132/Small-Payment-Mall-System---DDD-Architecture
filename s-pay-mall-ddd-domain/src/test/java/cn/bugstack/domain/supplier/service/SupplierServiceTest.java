package cn.bugstack.domain.supplier.service;

import cn.bugstack.domain.supplier.model.aggregate.SupplierAggregate;
import cn.bugstack.domain.supplier.repository.ISupplierRepository;
import cn.bugstack.types.exception.AppException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SupplierServiceTest {

    @Mock
    private ISupplierRepository supplierRepository;

    @InjectMocks
    private SupplierService supplierService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldAddSupplier() {
        SupplierAggregate supplier = supplier("S001", "supplier", 1);
        when(supplierRepository.save(supplier)).thenReturn(1L);

        assertEquals(Long.valueOf(1L), supplierService.addNewSupplier(supplier));
    }

    @Test(expected = AppException.class)
    public void shouldRejectInvalidStatus() {
        supplierService.addNewSupplier(supplier("S001", "supplier", 2));
    }

    @Test(expected = AppException.class)
    public void shouldRejectQueryWhenSupplierMissing() {
        when(supplierRepository.queryById(1L)).thenReturn(null);

        supplierService.querySupplierById(1L);
    }

    @Test
    public void shouldUpdateAndPreserveAuditFields() {
        LocalDateTime createTime = LocalDateTime.of(2026, 8, 1, 10, 0);
        SupplierAggregate current = SupplierAggregate.builder()
                .id(1L)
                .supplierCode("S001")
                .name("old")
                .status(1)
                .isDel(0)
                .createTime(createTime)
                .build();
        SupplierAggregate updated = supplier("S002", "new", 0);
        updated.setId(1L);
        when(supplierRepository.queryById(1L)).thenReturn(current);

        supplierService.updateSupplierById(updated);

        assertEquals(Integer.valueOf(0), updated.getIsDel());
        assertEquals(createTime, updated.getCreateTime());
        assertNotNull(updated.getUpdateTime());
        verify(supplierRepository).updateById(updated);
    }

    private SupplierAggregate supplier(String code, String name, Integer status) {
        return SupplierAggregate.builder()
                .supplierCode(code)
                .name(name)
                .status(status)
                .build();
    }
}
