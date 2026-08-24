package cn.bugstack.domain.groupbuy.repository;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTeamEntity;

public interface IGroupBuyTeamRepository {

    GroupBuyTeamEntity queryGroupBuyTeamByTeamId(String teamId);

    /**
     * 退单时扣减团队锁定人数（lock_count - 1）
     * @param teamId 团队ID
     * @return 更新行数
     */
    int updateSubtractLockCount(String teamId);

}
