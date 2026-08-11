package cn.bugstack.domain.groupbuy.repository;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyDiscountEntity;

public interface IGroupBuyDiscountRepository {

    GroupBuyDiscountEntity queryDiscountById(String discountId);
}
