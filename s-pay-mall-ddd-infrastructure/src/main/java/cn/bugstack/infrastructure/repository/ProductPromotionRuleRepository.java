package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.groupbuy.model.entity.ProductPromotionRuleEntity;
import cn.bugstack.domain.groupbuy.repository.IProductPromotionRuleRepository;
import cn.bugstack.infrastructure.dao.IProductPromotionRuleDao;
import cn.bugstack.infrastructure.dao.po.promotion.ProductPromotionRule;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ProductPromotionRuleRepository implements IProductPromotionRuleRepository {

    @Resource
    private IProductPromotionRuleDao productPromotionRuleDao;

    public List<ProductPromotionRuleEntity> queryProductPromotionRuleListByProductId(Long productId) {
        List<ProductPromotionRule> ruleList = productPromotionRuleDao.queryProductPromotionRuleListByProductId(productId);
        if (ruleList == null || ruleList.isEmpty()) {
            return Collections.emptyList();
        }
        return ruleList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    private ProductPromotionRuleEntity toEntity(ProductPromotionRule rule) {
        return ProductPromotionRuleEntity.builder()
                .id(rule.getId())
                .productId(rule.getProductId())
                .ruleType(rule.getRuleType())
                .ruleCode(rule.getRuleCode())
                .priority(rule.getPriority())
                .stackable(rule.getStackable())
                .status(rule.getStatus())
                .createTime(rule.getCreateTime())
                .updateTime(rule.getUpdateTime())
                .build();
    }
}
