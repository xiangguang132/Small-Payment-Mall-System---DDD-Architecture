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

    // 头像（完整 URL，如 http://localhost:8080/files/2026/09/06/uuid.png）
    private String avatar;

    // 手机号（脱敏）
    private String phoneMasked;

    // 角色：0顾客 1管理员 2原料库存管理人员
    private Integer role;

    // 角色名称
    private String roleName;

    // 账号资料是否已完善
    private Boolean profileCompleted;

}
