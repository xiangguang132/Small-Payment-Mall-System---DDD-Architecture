package cn.bugstack.domain.groupbuy.service.discount;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyDiscountEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class GroupBuyDiscountService implements IGroupBuyDiscountService {

    @Override
    public BigDecimal calculate(BigDecimal originalPrice, GroupBuyDiscountEntity groupBuyDiscount) {
        // TODO ZJ/MJ/ZK/N 策略后续按 marketPlan 接入
        return originalPrice;
    }

}
