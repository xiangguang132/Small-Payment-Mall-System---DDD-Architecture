package cn.bugstack.domain.groupbuy.service.discount.impl;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyDiscountEntity;
import cn.bugstack.domain.groupbuy.service.discount.AbstractGroupBuyDiscountService;
import cn.bugstack.types.common.Splits;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 满减实现类 - 继承 抽象折扣策略类
 * 10-1=9 满10-1
 * 10：originalPrice
 * 1：discount
 */
@Slf4j
@Service("MJ")
public class MJCalculateService extends AbstractGroupBuyDiscountService {

    @Override
    protected BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyDiscountEntity groupBuyDiscountEntity) {
        log.info("优惠策略折扣计算:{}", groupBuyDiscountEntity.getDiscountType());

        // 折扣
        String marketExpr = groupBuyDiscountEntity.getMarketExpr();
        String[] split = marketExpr.split(Splits.SPLIT);
        BigDecimal x = new BigDecimal(split[0].trim());
        BigDecimal y = new BigDecimal(split[1].trim());

        // 原价没达到满减门槛时，不享受优惠
        if (originalPrice.compareTo(x) < 0) {
            return originalPrice;
        }

        BigDecimal payPrice = originalPrice.subtract(y);
        if (payPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return new BigDecimal("0.01");
        }

        return payPrice;
    }
}
