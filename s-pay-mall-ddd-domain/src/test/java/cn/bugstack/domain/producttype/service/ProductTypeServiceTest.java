package cn.bugstack.domain.producttype.service;

import cn.bugstack.domain.producttype.model.aggregate.ProductTypeAggregate;
import cn.bugstack.domain.producttype.repository.IProductTypeRepository;
import cn.bugstack.types.exception.AppException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProductTypeServiceTest {

    @Mock
    private IProductTypeRepository productTypeRepository;

    @InjectMocks
    private ProductTypeService productTypeService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldAddProductType() {
        ProductTypeAggregate productType = type("P001", 1);
        when(productTypeRepository.save(productType)).thenReturn(1L);

        assertEquals(Long.valueOf(1L), productTypeService.addNewProductType(productType));
    }

    @Test(expected = AppException.class)
    public void shouldRejectDeleteWhenChildCategoryExists() {
        when(productTypeRepository.queryById(1L)).thenReturn(type("P001", 1));
        when(productTypeRepository.countProductByCategoryId(1L)).thenReturn(0L);
        when(productTypeRepository.countByParentId(1L)).thenReturn(2L);

        productTypeService.deleteProductTypeById(1L);
    }

    @Test(expected = AppException.class)
    public void shouldRejectUpdateWhenTypeCodeIsTaken() {
        ProductTypeAggregate current = ProductTypeAggregate.builder()
                .id(1L)
                .parentId(0L)
                .name("old")
                .typeCode("P001")
                .sort(1)
                .status(1)
                .build();
        ProductTypeAggregate updated = ProductTypeAggregate.builder()
                .id(1L)
                .parentId(0L)
                .name("new")
                .typeCode("P002")
                .sort(1)
                .status(1)
                .build();
        ProductTypeAggregate existed = ProductTypeAggregate.builder().id(2L).typeCode("P002").build();
        when(productTypeRepository.queryById(1L)).thenReturn(current);
        when(productTypeRepository.queryByTypeCode("P002")).thenReturn(existed);

        productTypeService.updateProductTypeById(updated);
    }

    @Test
    public void shouldToggleOnSaleStatus() {
        ProductTypeAggregate current = type("P001", 0);
        current.setId(1L);
        when(productTypeRepository.queryById(1L)).thenReturn(current);

        ProductTypeAggregate result = productTypeService.onSale(1L);

        assertEquals(Integer.valueOf(1), result.getStatus());
        verify(productTypeRepository).updateById(result);
    }

    private ProductTypeAggregate type(String code, Integer status) {
        return ProductTypeAggregate.builder()
                .parentId(0L)
                .name("product type")
                .typeCode(code)
                .sort(1)
                .status(status)
                .build();
    }
}
