package cn.bugstack.trigger.http;

import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.product.ProductDetailResponse;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTrialResult;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyActivityRepository;
import cn.bugstack.domain.groupbuy.service.trial.IGroupBuyTrialService;
import cn.bugstack.domain.product.model.aggregate.ProductAggregate;
import cn.bugstack.domain.product.service.IProductService;
import cn.bugstack.domain.producttype.service.IProductTypeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ProductControllerTest {

    @Mock
    private IProductService productService;
    @Mock
    private IGroupBuyActivityRepository groupBuyActivityRepository;
    @Mock
    private IProductTypeService productTypeService;
    @Mock
    private IGroupBuyTrialService groupBuyTrialService;

    @InjectMocks
    private ProductController productController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(productController, "fileBaseUrl", "http://localhost:8080");
    }

    @Test
    public void shouldReturnTrialPriceAndTargetCountOnDetail() throws Exception {
        Long productId = 19L;
        Long activityId = 10056L;
        ProductAggregate product = ProductAggregate.builder()
                .id(productId)
                .name("新鲜金针菇 500g")
                .price(new BigDecimal("6.90"))
                .coveringImg("/files/cover.png")
                .imgs("[]")
                .build();
        GroupBuyActivityEntity activity = GroupBuyActivityEntity.builder()
                .activityId(activityId)
                .productId(productId)
                .targetCount(5)
                .build();
        GroupBuyTrialResult trial = GroupBuyTrialResult.builder()
                .activityId(activityId)
                .productId(productId)
                .payPrice(new BigDecimal("5.90"))
                .targetCount(5)
                .build();

        when(productService.queryProductById(productId)).thenReturn(product);
        when(groupBuyActivityRepository.queryGroupBuyActivityByProductId(productId)).thenReturn(activity);
        when(groupBuyTrialService.queryGroupBuyTrial(any())).thenReturn(trial);

        Response<ProductDetailResponse> response = productController.detail(productId);

        assertEquals(activityId, response.getData().getActivityId());
        assertEquals(new BigDecimal("5.90"), response.getData().getTrialPayPrice());
        assertEquals(Integer.valueOf(5), response.getData().getTargetCount());
    }
}
