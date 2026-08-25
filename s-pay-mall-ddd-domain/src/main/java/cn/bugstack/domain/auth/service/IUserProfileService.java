package cn.bugstack.domain.auth.service;

import cn.bugstack.domain.auth.model.entity.UserEntity;

public interface IUserProfileService {

    /** 查询当前登录用户资料（不含密码） */
    UserEntity queryMe(String userId);

    /** 档案是否已完善：手机号与密码均已设置 */
    boolean isProfileCompleted(String userId);

    /**
     * 首次扫码注册后完善资料：绑定手机号、设置密码、更新昵称
     */
    void completeProfile(String userId, String phone, String password, String nickname);

}
