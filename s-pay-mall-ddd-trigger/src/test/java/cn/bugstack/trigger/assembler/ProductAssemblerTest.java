package cn.bugstack.trigger.assembler;

import cn.bugstack.api.request.product.ProductAddRequest;
import cn.bugstack.api.response.product.ProductDetailResponse;
import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ProductAssemblerTest {

    @Test
    public void shouldMapAddRequestToAggregate() {
        ProductAddRequest request = new ProductAddRequest();
        request.setName(" demo ");
        request.setDescription(" desc ");
        request.setSku(" SKU001 ");
        request.setCategoryId(1L);
        request.setStatus(1);
        request.setPrice(new BigDecimal("9.90"));

        ProductAggregate aggregate = ProductAssembler.toAggregate(request);

        assertEquals("demo", aggregate.getName());
        assertEquals("SKU001", aggregate.getSku());
        assertEquals(Long.valueOf(1L), aggregate.getCategoryId());
        assertEquals(new BigDecimal("9.90"), aggregate.getPrice());
    }

    @Test
    public void shouldMapAggregateToDetailResponse() {
        ProductAggregate aggregate = ProductAggregate.builder()
                .id(1L)
                .name("demo")
                .sku("SKU001")
                .categoryId(1L)
                .status(1)
                .price(new BigDecimal("9.90"))
                .coveringImg("https://example.com/cover.jpg")
                .imgs("[\"https://example.com/1.jpg\",\"https://example.com/2.jpg\"]")
                .build();

        ProductDetailResponse response = ProductAssembler.toDetailResponse(aggregate);

        assertEquals("demo", response.getName());
        assertEquals("SKU001", response.getSku());
        assertEquals(new BigDecimal("9.90"), response.getPrice());
        assertEquals("https://example.com/cover.jpg", response.getCoveringImg());
        List<String> expectedImgs = Arrays.asList("https://example.com/1.jpg", "https://example.com/2.jpg");
        assertEquals(expectedImgs, response.getImgs());
    }
}
