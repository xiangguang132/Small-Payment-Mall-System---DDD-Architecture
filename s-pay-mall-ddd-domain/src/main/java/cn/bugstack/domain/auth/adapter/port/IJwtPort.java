package cn.bugstack.domain.auth.adapter.port;

public interface IJwtPort {

    String createToken(String openid);

    String parseOpenid(String token);

    boolean verifyToken(String token);

}
