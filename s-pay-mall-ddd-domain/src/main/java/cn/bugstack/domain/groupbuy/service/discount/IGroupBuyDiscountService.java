package cn.bugstack.domain.groupbuy.service.discount;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyDiscountEntity;

import java.math.BigDecimal;

public interface IGroupBuyDiscountService {

    BigDecimal calculate(String userId, BigDecimal originalPrice, GroupBuyDiscountEntity groupBuyDiscount);

}
