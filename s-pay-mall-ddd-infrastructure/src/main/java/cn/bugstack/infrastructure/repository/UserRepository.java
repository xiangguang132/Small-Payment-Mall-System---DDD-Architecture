package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.auth.adapter.repository.IUserRepository;
import cn.bugstack.domain.auth.model.entity.UserEntity;
import cn.bugstack.infrastructure.dao.IUserDao;
import cn.bugstack.infrastructure.dao.po.user.User;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class UserRepository implements IUserRepository {

    @Resource
    private IUserDao userDao;

    @Override
    public void save(UserEntity userEntity) {
        try {
            userDao.insert(User.builder()
                    .userId(userEntity.getUserId())
                    .password(userEntity.getPassword())
                    .nickname(userEntity.getNickname())
                    .avatar(userEntity.getAvatar())
                    .phone(userEntity.getPhone())
                    .role(userEntity.getRole())
                    .build());
        } catch (Exception e) {
            throw new AppException(ResponseCode.UN_ERROR, "保存用户失败", e);
        }
    }

    @Override
    public UserEntity queryByUserId(String userId) {
        User user = userDao.queryByUserId(userId);
        return toEntity(user);
    }

    @Override
    public UserEntity queryByAccount(String account) {
        return toEntity(userDao.queryByAccount(account));
    }

    @Override
    public void updateProfile(UserEntity userEntity) {
        try {
            userDao.updateProfile(User.builder()
                    .userId(userEntity.getUserId())
                    .phone(userEntity.getPhone())
                    .password(userEntity.getPassword())
                    .nickname(userEntity.getNickname())
                    .build());
        } catch (Exception e) {
            throw new AppException(ResponseCode.CONFLICT, "完善资料失败（手机号可能已被绑定）", e);
        }
    }

    @Override
    public void updateInfo(UserEntity userEntity) {
        try {
            userDao.updateInfo(User.builder()
                    .userId(userEntity.getUserId())
                    .password(userEntity.getPassword())
                    .nickname(userEntity.getNickname())
                    .avatar(userEntity.getAvatar())
                    .build());
        } catch (Exception e) {
            throw new AppException(ResponseCode.UN_ERROR, "修改用户信息失败", e);
        }
    }

    private UserEntity toEntity(User user) {
        if (user == null) {
            return null;
        }
        return UserEntity.builder()
                .userId(user.getUserId())
                .password(user.getPassword())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();
    }

    @Override
    public void updateRole(String userId, Integer role) {
        try {
            userDao.updateRole(User.builder()
                    .userId(userId)
                    .role(role)
                    .build());
        } catch (Exception e) {
            throw new AppException(ResponseCode.UN_ERROR, "修改用户角色失败", e);
        }
    }

}
