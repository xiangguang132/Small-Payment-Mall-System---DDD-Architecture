package cn.bugstack.api.request.user;

import lombok.Data;

/**
 * 修改用户信息：所有字段均为可选，仅更新传入的字段；手机号不可修改
 */
@Data
public class UpdateProfileRequest {

    // 昵称，可选
    private String nickname;

    // 头像，可选
    private String avatar;

    // 密码，可选（明文，服务端加密存储）
    private String password;

    // 确认密码，可选；传入 password 时必须与其一致
    private String confirmPassword;

}
