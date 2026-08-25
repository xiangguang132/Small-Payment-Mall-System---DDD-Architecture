package cn.bugstack.domain.auth.service;

import cn.bugstack.domain.auth.adapter.port.IPasswordEncoder;
import cn.bugstack.domain.auth.adapter.repository.IUserRepository;
import cn.bugstack.domain.auth.model.entity.UserEntity;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.regex.Pattern;

/**
 * 用户资料服务：首次扫码注册后的资料完善、当前用户查询
 */
@Service
public class UserProfileService implements IUserProfileService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final int PASSWORD_MIN_LENGTH = 6;

    @Resource
    private IUserRepository userRepository;
    @Resource
    private IPasswordEncoder passwordEncoder;

    @Override
    public UserEntity queryMe(String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new AppException(ResponseCode.NO_LOGIN);
        }
        UserEntity userEntity = userRepository.queryByUserId(userId);
        if (userEntity == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "用户不存在");
        }
        // 不回传密码哈希
        userEntity.setPassword(null);
        return userEntity;
    }

    @Override
    public boolean isProfileCompleted(String userId) {
        if (StringUtils.isBlank(userId)) {
            return false;
        }
        UserEntity userEntity = userRepository.queryByUserId(userId);
        return userEntity != null
                && StringUtils.isNotBlank(userEntity.getPhone())
                && StringUtils.isNotBlank(userEntity.getPassword());
    }

    @Override
    public void completeProfile(String userId, String phone, String password, String nickname) {
        validatePhone(phone);
        validatePassword(password);

        UserEntity userEntity = userRepository.queryByUserId(userId);
        if (userEntity == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "用户不存在，请先扫码登录");
        }
        if (isProfileCompleted(userId)) {
            throw new AppException(ResponseCode.CONFLICT, "账号资料已完善，无需重复设置");
        }
        // 手机号唯一性预检（数据库 uk_phone 兜底）
        UserEntity phoneOwner = userRepository.queryByAccount(phone);
        if (phoneOwner != null && !phoneOwner.getUserId().equals(userId)) {
            throw new AppException(ResponseCode.CONFLICT, "该手机号已被其他账号绑定");
        }

        userRepository.updateProfile(UserEntity.builder()
                .userId(userId)
                .phone(phone)
                .password(passwordEncoder.encode(password))
                .nickname(StringUtils.defaultIfBlank(nickname, userEntity.getNickname()))
                .build());
    }

    public static void validatePhone(String phone) {
        if (StringUtils.isBlank(phone) || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "手机号格式不正确");
        }
    }

    public static void validatePassword(String password) {
        if (StringUtils.isBlank(password) || password.length() < PASSWORD_MIN_LENGTH) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "密码不能为空且至少" + PASSWORD_MIN_LENGTH + "位");
        }
    }

}
