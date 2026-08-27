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

    /**
     * 团空即关：团内无有效订单（status∈(0,1)）时关闭队伍（status=2）
     * 单条原子 UPDATE，并发退出场景下不漏关、不误关
     * @param teamId 团队ID
     * @return 更新行数（1=本次关闭成功，0=未关闭）
     */
    int updateStatus2CloseIfEmpty(String teamId);

}
