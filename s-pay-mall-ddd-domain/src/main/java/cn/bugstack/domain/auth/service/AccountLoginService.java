package cn.bugstack.domain.auth.service;

import cn.bugstack.domain.auth.adapter.port.IJwtPort;
import cn.bugstack.domain.auth.adapter.port.IPasswordEncoder;
import cn.bugstack.domain.auth.adapter.repository.IUserRepository;
import cn.bugstack.domain.auth.model.entity.UserEntity;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 账号密码登录服务
 */
@Service
public class AccountLoginService implements IAccountLoginService {

    @Resource
    private IUserRepository userRepository;
    @Resource
    private IPasswordEncoder passwordEncoder;
    @Resource
    private IJwtPort jwtPort;

    @Override
    public void register(String userId, String password, String nickname) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(password)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "账号或密码为空");
        }
        // 账号统一使用手机号
        UserProfileService.validatePhone(userId);
        UserProfileService.validatePassword(password);
        if (userRepository.queryByAccount(userId) != null) {
            throw new AppException(ResponseCode.CONFLICT, "该手机号已注册");
        }
        userRepository.save(UserEntity.builder()
                .userId(userId)
                .password(passwordEncoder.encode(password))
                .nickname(StringUtils.defaultIfBlank(nickname, maskPhone(userId)))
                .phone(userId)
                .build());
    }

    @Override
    public String login(String userId, String password) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(password)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "账号或密码为空");
        }
        // 支持手机号或 user_id 登录
        UserEntity userEntity = userRepository.queryByAccount(userId);
        if (userEntity == null || StringUtils.isBlank(userEntity.getPassword())
                || !passwordEncoder.matches(password, userEntity.getPassword())) {
            throw new AppException(ResponseCode.NO_LOGIN, "账号或密码错误");
        }
        return jwtPort.createToken(userEntity.getUserId());
    }

    private String maskPhone(String phone) {
        return phone.length() == 11 ? phone.substring(0, 3) + "****" + phone.substring(7) : phone;
    }

}
