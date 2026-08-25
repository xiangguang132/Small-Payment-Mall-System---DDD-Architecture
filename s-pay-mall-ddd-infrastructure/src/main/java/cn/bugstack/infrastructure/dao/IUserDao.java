package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IUserDao {

    void insert(User user);

    User queryByUserId(@Param("userId") String userId);

}
