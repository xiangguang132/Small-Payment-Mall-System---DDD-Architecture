package cn.bugstack.domain.groupbuy.service.trial.rule;

import cn.bugstack.domain.groupbuy.model.entity.TrialRuleResult;
import cn.bugstack.domain.groupbuy.model.valobj.TrialRuleTypeEnum;

public interface ITrialRule {

    TrialRuleTypeEnum getRuleType();

    boolean match(TrialRuleContext context);

    TrialRuleResult calculate(TrialRuleContext context);

}
