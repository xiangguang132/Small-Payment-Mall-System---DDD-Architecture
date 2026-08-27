package cn.bugstack.domain.groupbuy.service.trial.rule;

import cn.bugstack.domain.groupbuy.model.entity.ProductPromotionRuleEntity;
import cn.bugstack.domain.groupbuy.model.entity.TrialRuleResult;
import cn.bugstack.domain.groupbuy.model.valobj.TrialRuleTypeEnum;
import cn.bugstack.domain.groupbuy.repository.IProductPromotionRuleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TrialRuleChain {

    private final IProductPromotionRuleRepository productPromotionRuleRepository;
    private final List<ITrialRule> trialRuleList;

    public TrialRuleContext execute(TrialRuleContext context) {
        if (context == null || context.getProductId() == null) {
            return context;
        }

        if (trialRuleList == null || trialRuleList.isEmpty()) {
            return context;
        }

        Map<TrialRuleTypeEnum, ITrialRule> ruleHandlerMap = trialRuleList.stream()
                .collect(Collectors.toMap(
                        ITrialRule::getRuleType,
                        rule -> rule,
                        (first, second) -> first
                ));

        List<ProductPromotionRuleEntity> ruleEntityList = productPromotionRuleRepository.queryProductPromotionRuleListByProductId(context.getProductId());
        if (ruleEntityList == null || ruleEntityList.isEmpty()) {
            return context;
        }

        ruleEntityList.sort(Comparator.comparing(ProductPromotionRuleEntity::getPriority,
                Comparator.nullsLast(Integer::compareTo)));

        for (ProductPromotionRuleEntity rule : ruleEntityList) {
            if (rule == null || rule.getRuleType() == null) {
                continue;
            }

            ITrialRule handler = ruleHandlerMap.get(TrialRuleTypeEnum.get(rule.getRuleType()));
            if (handler == null) {
                continue;
            }

            if (!handler.match(context)) {
                continue;
            }

            TrialRuleResult result = handler.calculate(context);
            context.apply(result);

            if (result != null && Boolean.FALSE.equals(result.getStackable())) {
                break;
            }
        }
        return context;
    }

}