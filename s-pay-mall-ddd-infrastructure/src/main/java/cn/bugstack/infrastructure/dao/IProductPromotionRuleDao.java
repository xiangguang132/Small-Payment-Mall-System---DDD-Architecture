package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.promotion.ProductPromotionRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IProductPromotionRuleDao {

    List<ProductPromotionRule> queryProductPromotionRuleListByProductId(@Param("productId") Long productId);

}
