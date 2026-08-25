package cn.bugstack.domain.auth.service;

import cn.bugstack.domain.auth.adapter.port.IJwtPort;
import cn.bugstack.domain.auth.adapter.port.ILoginPort;
import cn.bugstack.domain.auth.adapter.repository.IUserRepository;
import cn.bugstack.domain.auth.model.entity.UserEntity;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import com.google.common.cache.Cache;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 微信服务
 */
@Service
public class WeixinLoginService implements ILoginService {

    @Resource
    private ILoginPort loginPort;
    @Resource
    private IJwtPort jwtPort;
    @Resource
    private IUserRepository userRepository;
    @Resource
    private Cache<String, String> openidToken;

    @Override
    public String createQrCodeTicket() {
        try {
            return loginPort.createQrCodeTicket();
        } catch (Exception e) {
            throw new AppException(ResponseCode.UN_ERROR, e.getMessage(), e);
        }
    }

    @Override
    public String checkLogin(String ticket) {
        // 通过 ticket 判断，用户是否登录。如果登录了，会在内存里写入信息
        // return openidToken.getIfPresent(ticket);
        String openid = openidToken.getIfPresent(ticket);
        if (openid == null) {
            return null;
        }
        String token = jwtPort.createToken(openid);
        openidToken.invalidate(ticket);
        return token;
    }

    @Override
    public void saveLoginState(String ticket, String openid) throws IOException {
        openidToken.put(ticket, openid);
        // 首次扫码自动建立用户档案
        if (userRepository.queryByUserId(openid) == null) {
            userRepository.save(UserEntity.builder()
                    .userId(openid)
                    .nickname("微信用户")
                    .build());
        }
        loginPort.sendLoginTempleteMessage(openid);
    }

}
