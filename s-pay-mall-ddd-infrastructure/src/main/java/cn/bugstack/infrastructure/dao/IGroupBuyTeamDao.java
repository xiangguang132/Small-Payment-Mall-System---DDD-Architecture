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
}
