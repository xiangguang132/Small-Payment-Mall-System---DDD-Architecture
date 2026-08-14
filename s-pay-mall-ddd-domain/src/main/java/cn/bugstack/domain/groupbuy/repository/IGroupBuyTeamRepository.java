package cn.bugstack.domain.groupbuy.repository;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTeamEntity;

public interface IGroupBuyTeamRepository {

    GroupBuyTeamEntity queryGroupBuyTeamByTeamId(String teamId);

}
