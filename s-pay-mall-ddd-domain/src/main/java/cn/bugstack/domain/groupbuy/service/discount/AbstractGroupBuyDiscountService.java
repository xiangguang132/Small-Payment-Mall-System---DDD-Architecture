package cn.bugstack.domain.groupbuy.service.discount;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyDiscountEntity;
import cn.bugstack.domain.groupbuy.model.valobj.DiscountTypeEnum;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyActivityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
@Service
public abstract class AbstractGroupBuyDiscountService implements IGroupBuyDiscountService {

    @Resource
    private IGroupBuyActivityRepository repository;

    @Override
    public BigDecimal calculate(String userId, BigDecimal originalPrice, GroupBuyDiscountEntity groupBuyDiscount) {
        // TODO ZJ/MJ/ZK/N 策略后续按 marketPlan 接入
        if (DiscountTypeEnum.TAG.equals(groupBuyDiscount.getDiscountType())) {
            boolean isCrowdRange = filterTagId(userId, groupBuyDiscount.getTagId());
            if (!isCrowdRange) {
                log.info("折扣优惠计算被拦截，用户不属于优惠人群标签范围内");
                return originalPrice;
            }

            return doCalculate(originalPrice, groupBuyDiscount);
        }

        return doCalculate(originalPrice, groupBuyDiscount);
    }

    private boolean filterTagId(String userId, String tagId) {
        return repository.withinTagCrowdRange(tagId, userId);
    }

    // doCalculate
    protected abstract BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyDiscountEntity groupBuyDiscountEntity);

}
