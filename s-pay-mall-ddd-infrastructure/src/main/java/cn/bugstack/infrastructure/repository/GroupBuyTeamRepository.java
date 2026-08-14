package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTeamEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyTeamRepository;
import cn.bugstack.infrastructure.dao.IGroupBuyTeamDao;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class GroupBuyTeamRepository implements IGroupBuyTeamRepository {

    @Resource
    private IGroupBuyTeamDao groupBuyTeamDao;

    @Override
    public GroupBuyTeamEntity queryGroupBuyTeamByTeamId(String teamId) {
        if (teamId == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "拼团队伍id不能为空");
        }
        return groupBuyTeamDao.queryGroupBuyTeamByTeamId(teamId);

    }
}
