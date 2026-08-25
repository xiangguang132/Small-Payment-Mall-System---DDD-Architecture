package cn.bugstack.api.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    // 用户ID（微信用户为openid）
    private String userId;

    // 昵称
    private String nickname;

    // 手机号（脱敏）
    private String phoneMasked;

    // 账号资料是否已完善
    private Boolean profileCompleted;

}
