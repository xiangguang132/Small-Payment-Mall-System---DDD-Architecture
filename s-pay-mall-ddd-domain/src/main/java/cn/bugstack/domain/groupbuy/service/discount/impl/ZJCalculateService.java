package cn.bugstack.domain.groupbuy.service.discount.impl;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyDiscountEntity;
import cn.bugstack.domain.groupbuy.service.discount.AbstractGroupBuyDiscountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 直减实现类 - 继承 抽象折扣策略类
 * marketExpr 促销要减去的价格
 * discount 格式化后的减价
 * originalPrice 原价
 * payPrice 支付价格
 */
@Slf4j
@Service("ZJ")
public class ZJCalculateService extends AbstractGroupBuyDiscountService {

    @Override
    protected BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyDiscountEntity groupBuyDiscountEntity) {
        log.info("优惠策略折扣计算:{}", groupBuyDiscountEntity.getDiscountType());

        // 折扣表达式
        String marketExpr = groupBuyDiscountEntity.getMarketExpr();
        BigDecimal discount = new BigDecimal(marketExpr.trim());

        BigDecimal payPrice = originalPrice.subtract(discount);
        if (payPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return new BigDecimal("0.01");
        }

        return payPrice;
    }
}
