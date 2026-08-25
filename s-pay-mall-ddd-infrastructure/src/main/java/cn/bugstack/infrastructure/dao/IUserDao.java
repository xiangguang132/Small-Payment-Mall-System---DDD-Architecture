package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IUserDao {

    void insert(User user);

    User queryByUserId(@Param("userId") String userId);

    /** 按 账号(user_id) 或 手机号 查询 */
    User queryByAccount(@Param("account") String account);

    /** 完善资料：绑定手机号、设置密码、更新昵称 */
    int updateProfile(User user);

    /** 修改用户信息：仅更新非空字段，手机号不可修改 */
    int updateInfo(User user);

}
