-- 用户优惠券表
CREATE TABLE IF NOT EXISTS `group_buy_coupon_user` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `coupon_user_id` varchar(64) NOT NULL COMMENT '用户券记录ID',
    `user_id` varchar(64) NOT NULL COMMENT '用户ID',
    `coupon_id` varchar(64) NOT NULL COMMENT '优惠券ID',
    `source_order_no` varchar(64) DEFAULT NULL COMMENT '来源单号',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0未使用 1已使用 2已过期 3已失效',
    `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
    `used_time` datetime DEFAULT NULL COMMENT '使用时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_coupon_user_id` (`coupon_user_id`),
    KEY `idx_user_status` (`user_id`, `status`),
    KEY `idx_coupon_id` (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';
