package cn.bugstack.infrastructure.repository;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyDiscountEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyDiscountRepository;
import cn.bugstack.infrastructure.dao.IGroupBuyDiscountDao;
import cn.bugstack.infrastructure.dao.po.groupbuy.GroupBuyDiscount;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class GroupBuyDiscountRepository implements IGroupBuyDiscountRepository {

    @Resource
    private IGroupBuyDiscountDao groupBuyDiscountDao;

    @Override
    public GroupBuyDiscountEntity queryDiscountById(String discountId) {
        GroupBuyDiscount discount =
                groupBuyDiscountDao.queryGroupBuyActivityDiscountByDiscountId(discountId);
        if (discount == null) {
            return null;
        }

        return GroupBuyDiscountEntity.builder()
                .id(discount.getId())
                .discountId(discount.getDiscountId())
                .discountName(discount.getDiscountName())
                .discountDesc(discount.getDiscountDesc())
                .discountType(discount.getDiscountType())
                .marketPlan(discount.getMarketPlan())
                .marketExpr(discount.getMarketExpr())
                .tagId(discount.getTagId())
                .createTime(discount.getCreateTime())
                .updateTime(discount.getUpdateTime())
                .build();
    }
}
