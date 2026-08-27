package cn.bugstack.infrastructure.dao.po.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String userId;
    private String password;     // BCrypt哈希；微信用户为NULL
    private String nickname;
    private String avatar;
    private String phone;
    private Integer status;      // 0禁用 1正常
    private Integer role;        // 0顾客 1管理员 2原料库存管理人员
    private Date createTime;
    private Date updateTime;

}
