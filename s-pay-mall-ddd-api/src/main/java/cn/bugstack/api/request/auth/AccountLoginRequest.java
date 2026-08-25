package cn.bugstack.api.request.auth;

import lombok.Data;

@Data
public class AccountLoginRequest {

    // 用户ID（注册用户名）
    private String userId;

    // 密码
    private String password;

}
