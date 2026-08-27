package cn.bugstack.domain.auth.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    private String userId;
    private String password;
    private String nickname;
    private String avatar;
    private String phone;
    private Integer role;        // 0顾客 1管理员 2原料库存管理人员

}
