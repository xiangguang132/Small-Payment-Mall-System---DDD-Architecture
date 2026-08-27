package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.groupbuy.repository.ITrialSwitchRepository;
import cn.bugstack.infrastructure.dcc.DCCService;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class TrialSwitchRepository implements ITrialSwitchRepository {

    @Resource
    private DCCService dccService;

    public boolean downgradeSwitch() {
        return dccService.isDowngradeSwitch();
    }

    public boolean cutRange(String userId) {
        return dccService.isCutRange(userId);
    }
}
