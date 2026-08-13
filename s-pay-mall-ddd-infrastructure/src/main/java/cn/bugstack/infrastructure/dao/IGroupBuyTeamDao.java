package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.groupbuy.GroupBuyTeam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IGroupBuyTeamDao {

    void insert(GroupBuyTeam team);

    int updateAddLockCount(@Param("teamId") String teamId);
}
