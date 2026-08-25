package cn.bugstack.api.request.auth;

import lombok.Data;

@Data
public class AccountRegisterRequest {

    // 用户ID（注册用户名）
    private String userId;

    // 密码
    private String password;

    // 昵称，可空
    private String nickname;

}
