-- ============================================================
-- 用户表
-- 库：s-pay-mall / s-pay-mall-test
-- user_id：微信扫码登录 = openid；账号密码登录 = 注册用户名
-- password：BCrypt 哈希，微信用户为 NULL
-- 用法：mysql -uroot -p --default-character-set=utf8mb4 <库名> < user.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS `user` (
    `id`          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `user_id`     varchar(64)     NOT NULL COMMENT '用户ID；微信登录=openid，账号登录=用户名',
    `password`    varchar(100)             DEFAULT NULL COMMENT '密码哈希(BCrypt)；微信用户为NULL',
    `nickname`    varchar(64)              DEFAULT NULL COMMENT '昵称',
    `avatar`      varchar(256)             DEFAULT NULL COMMENT '头像URL',
    `phone`       varchar(20)              DEFAULT NULL COMMENT '手机号',
    `status`      tinyint         NOT NULL DEFAULT '1' COMMENT '状态；0禁用、1正常',
    `role`        tinyint         NOT NULL DEFAULT '0' COMMENT '角色；0顾客、1管理员、2原料库存管理人员',
    `create_time` datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_user_id` (`user_id`) USING BTREE,
    UNIQUE KEY `uk_phone` (`phone`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC COMMENT ='用户表';
