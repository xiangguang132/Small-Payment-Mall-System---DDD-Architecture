package cn.bugstack.api.request.user;

import lombok.Data;

@Data
public class CompleteProfileRequest {

    // 手机号（作为账号）
    private String phone;

    // 密码
    private String password;

    // 昵称，可空
    private String nickname;

}
