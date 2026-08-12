package cn.bugstack.domain.groupbuy.service.discount.impl;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyDiscountEntity;
import cn.bugstack.domain.groupbuy.service.discount.AbstractGroupBuyDiscountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * N元购实现类 - 继承 抽象折扣策略类
 * 直接替代原价
 */
@Slf4j
@Service("N")
public class NCalculateService extends AbstractGroupBuyDiscountService {

    @Override
    protected BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyDiscountEntity groupBuyDiscountEntity) {
        log.info("优惠策略折扣计算:{}", groupBuyDiscountEntity.getDiscountType());

        // 折扣表达式
        String marketExpr = groupBuyDiscountEntity.getMarketExpr();
        // N元购 -----> 直接替代原价
        return new BigDecimal(marketExpr);
    }
}
