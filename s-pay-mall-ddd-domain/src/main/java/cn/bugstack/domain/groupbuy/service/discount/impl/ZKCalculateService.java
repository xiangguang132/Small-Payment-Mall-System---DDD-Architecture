package cn.bugstack.domain.groupbuy.service.discount.impl;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyDiscountEntity;
import cn.bugstack.domain.groupbuy.service.discount.AbstractGroupBuyDiscountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 折扣实现类 - 继承 抽象折扣策略类
 * 10*0.9=9
 * 10：originalPrice
 * 0.9(90%)：discount
 * 9：payPrice
 */
@Slf4j
@Service("ZK")
public class ZKCalculateService extends AbstractGroupBuyDiscountService {

    @Override
    protected BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyDiscountEntity groupBuyDiscountEntity) {
        log.info("优惠策略折扣计算:{}", groupBuyDiscountEntity.getDiscountType());

        // 表达式 -> 这里即90% 10% 的打折汇率
        String marketExpr = groupBuyDiscountEntity.getMarketExpr();

        // 格式化后的折扣
        BigDecimal discount = new BigDecimal(marketExpr.trim());

        // 支付价格
        BigDecimal payPrice = originalPrice.multiply(discount);

        if (payPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return new BigDecimal("0.01");
        }

        return payPrice;
    }
}
