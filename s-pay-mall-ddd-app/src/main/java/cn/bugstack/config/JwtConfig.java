package cn.bugstack.config;

import lombok.Data;

@Data
public class JwtConfig {

    private String secret;
    private String issuer;
    private Long expireSeconds;
}
