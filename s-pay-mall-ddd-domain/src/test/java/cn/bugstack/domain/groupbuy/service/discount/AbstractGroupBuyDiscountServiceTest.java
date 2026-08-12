package cn.bugstack.domain.groupbuy.service.discount;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyDiscountEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyActivityRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AbstractGroupBuyDiscountServiceTest {

    @Mock
    private IGroupBuyActivityRepository repository;

    private AbstractGroupBuyDiscountService service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new AbstractGroupBuyDiscountService() {
            @Override
            protected BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyDiscountEntity groupBuyDiscountEntity) {
                return originalPrice.add(BigDecimal.ONE);
            }
        };
        ReflectionTestUtils.setField(service, "repository", repository);
    }

    @Test
    public void shouldApplyTagDiscountWhenUserInRange() {
        when(repository.withinTagCrowdRange("tag-1", "u1")).thenReturn(true);

        BigDecimal result = service.calculate(
                "u1",
                new BigDecimal("100.00"),
                GroupBuyDiscountEntity.builder().discountType(1).tagId("tag-1").build()
        );

        assertEquals(new BigDecimal("101.00"), result);
    }

    @Test
    public void shouldReturnOriginalPriceWhenTagUserOutOfRange() {
        when(repository.withinTagCrowdRange("tag-1", "u1")).thenReturn(false);

        BigDecimal result = service.calculate(
                "u1",
                new BigDecimal("100.00"),
                GroupBuyDiscountEntity.builder().discountType(1).tagId("tag-1").build()
        );

        assertEquals(new BigDecimal("100.00"), result);
        verify(repository).withinTagCrowdRange("tag-1", "u1");
    }

    @Test
    public void shouldSkipTagFilterForBaseDiscount() {
        BigDecimal result = service.calculate(
                "u1",
                new BigDecimal("100.00"),
                GroupBuyDiscountEntity.builder().discountType(0).build()
        );

        assertEquals(new BigDecimal("101.00"), result);
        verify(repository, never()).withinTagCrowdRange("tag-1", "u1");
    }
}
