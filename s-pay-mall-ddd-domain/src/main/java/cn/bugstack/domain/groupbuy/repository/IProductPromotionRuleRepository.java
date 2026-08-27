package cn.bugstack.domain.groupbuy.repository;

import cn.bugstack.domain.groupbuy.model.entity.ProductPromotionRuleEntity;

import java.util.List;

public interface IProductPromotionRuleRepository {

    List<ProductPromotionRuleEntity> queryProductPromotionRuleListByProductId(Long productId);
}
