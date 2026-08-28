-- 拼团优惠券表
CREATE TABLE IF NOT EXISTS `group_buy_coupon` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `coupon_id` varchar(64) NOT NULL COMMENT '优惠券ID',
    `coupon_name` varchar(128) NOT NULL COMMENT '优惠券名称',
    `coupon_type` varchar(32) NOT NULL COMMENT '优惠券类型：FULL_REDUCTION / DISCOUNT / FIXED',
    `threshold_amount` decimal(18,2) DEFAULT NULL COMMENT '门槛金额',
    `discount_amount` decimal(18,2) DEFAULT NULL COMMENT '优惠金额（FIXED/FULL_REDUCTION 使用）',
    `discount_rate` decimal(10,4) DEFAULT NULL COMMENT '折扣率，例如 0.9000（DISCOUNT 使用）',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
    `start_time` datetime DEFAULT NULL COMMENT '生效开始时间',
    `end_time` datetime DEFAULT NULL COMMENT '生效结束时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_coupon_id` (`coupon_id`),
    KEY `idx_status_time` (`status`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼团优惠券表';
