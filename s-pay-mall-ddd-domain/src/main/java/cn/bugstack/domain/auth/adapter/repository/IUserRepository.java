package cn.bugstack.domain.auth.adapter.repository;

import cn.bugstack.domain.auth.model.entity.UserEntity;

public interface IUserRepository {

    void save(UserEntity userEntity);

    UserEntity queryByUserId(String userId);

    /** 按 账号(user_id) 或 手机号 查询 */
    UserEntity queryByAccount(String account);

    /** 完善资料：绑定手机号、设置密码、更新昵称 */
    void updateProfile(UserEntity userEntity);

}
