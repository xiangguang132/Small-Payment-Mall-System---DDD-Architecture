package cn.bugstack.domain.auth.adapter.repository;

import cn.bugstack.domain.auth.model.entity.UserEntity;

public interface IUserRepository {

    void save(UserEntity userEntity);

    UserEntity queryByUserId(String userId);

}
