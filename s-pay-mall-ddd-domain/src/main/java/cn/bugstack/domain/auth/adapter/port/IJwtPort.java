package cn.bugstack.domain.auth.adapter.port;

public interface IJwtPort {

    String createToken(String userId);

    String parseUserId(String token);

    boolean verifyToken(String token);

}
