package cn.bugstack.domain.product.service;

import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import cn.bugstack.domain.product.repository.IProductRepository;
import cn.bugstack.domain.producttype.model.aggregate.ProductTypeAggregate;
import cn.bugstack.domain.producttype.repository.IProductTypeRepository;
import cn.bugstack.types.exception.AppException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProductServiceTest {

    @Mock
    private IProductRepository productRepository;

    @Mock
    private IProductTypeRepository productTypeRepository;

    @InjectMocks
    private ProductService productService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldAddProductWhenCategoryEnabled() {
        ProductAggregate product = ProductAggregate.builder()
                .name("demo")
                .sku("SKU001")
                .categoryId(1L)
                .status(1)
                .price(new BigDecimal("9.90"))
                .build();
        when(productTypeRepository.queryById(1L)).thenReturn(enabledCategory());
        when(productRepository.save(product)).thenReturn(1L);

        assertEquals(Long.valueOf(1L), productService.addNewProduct(product));
    }

    @Test(expected = AppException.class)
    public void shouldRejectAddWhenCategoryMissing() {
        ProductAggregate product = ProductAggregate.builder()
                .name("demo")
                .sku("SKU001")
                .categoryId(1L)
                .status(1)
                .build();
        when(productTypeRepository.queryById(1L)).thenReturn(null);

        productService.addNewProduct(product);
    }

    @Test
    public void shouldToggleOnSaleStatus() {
        ProductAggregate current = ProductAggregate.builder()
                .id(1L)
                .name("demo")
                .sku("SKU001")
                .categoryId(1L)
                .status(0)
                .price(new BigDecimal("9.90"))
                .isDel(0)
                .build();
        when(productRepository.queryById(1L)).thenReturn(current);

        ProductAggregate result = productService.onSale(1L);

        assertEquals(Integer.valueOf(1), result.getStatus());
        verify(productRepository).updateById(result);
    }

    private ProductTypeAggregate enabledCategory() {
        return ProductTypeAggregate.builder().id(1L).status(1).build();
    }
}
