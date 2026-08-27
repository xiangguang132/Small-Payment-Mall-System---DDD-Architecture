package cn.bugstack.domain.auth.adapter.port;

public interface IJwtPort {

    /** 生成 Token（携带 userId 和 role） */
    String createToken(String userId, Integer role);

    /** 校验 Token 是否有效 */
    boolean verifyToken(String token);

    /** 从 Token 解析 userId */
    String parseUserId(String token);

    /** 从 Token 解析 role */
    Integer parseRole(String token);

}
