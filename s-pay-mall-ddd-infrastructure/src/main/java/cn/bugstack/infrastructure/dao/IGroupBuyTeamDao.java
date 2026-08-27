package cn.bugstack.infrastructure.dao;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyTeamEntity;
import cn.bugstack.infrastructure.dao.po.groupbuy.GroupBuyTeam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IGroupBuyTeamDao {

    void insert(GroupBuyTeam team);

    int updateAddLockCount(@Param("teamId") String teamId);

    int updateAddCompleteCount(@Param("teamId") String teamId);

    int updateStatus2Complete(@Param("teamId") String teamId);

    GroupBuyTeamEntity queryGroupBuyTeamByTeamId(@Param("teamId") String teamId);

    int updateSubtractLockCount(@Param("teamId") String teamId);

    /**
     * 团空即关：团内无有效订单时关闭队伍（status=2）
     * @param teamId 团队ID
     * @return 更新行数（1=已关闭，0=团内仍有有效成员或已关闭）
     */
    int updateStatus2CloseIfEmpty(@Param("teamId") String teamId);
}
