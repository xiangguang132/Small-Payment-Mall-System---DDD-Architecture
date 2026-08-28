-- 商品优惠规则映射表
CREATE TABLE IF NOT EXISTS `product_promotion_rule` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `rule_type` int NOT NULL COMMENT '规则类型：0=GROUP_BUY 1=COUPON 2=POINTS',
    `rule_code` varchar(64) NOT NULL COMMENT '规则编码：拼团discountId、券couponId或积分配置编码',
    `priority` int NOT NULL DEFAULT 0 COMMENT '执行顺序，越小越优先',
    `stackable` tinyint NOT NULL DEFAULT 1 COMMENT '是否可叠加：0否 1是',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品优惠规则映射表';

-- 种子数据：商品1(拼团测试商品) 走拼团9折优惠(GB_DISCOUNT_ZK)
INSERT INTO product_promotion_rule (product_id, rule_type, rule_code, priority, stackable, status)
SELECT 1, 0, 'GB_DISCOUNT_ZK', 0, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM product_promotion_rule WHERE product_id = 1 AND rule_code = 'GB_DISCOUNT_ZK');

-- 种子数据：商品1(拼团测试商品) 支持优惠券抵扣(COUPON)，优先级在拼团折扣之后
INSERT INTO product_promotion_rule (product_id, rule_type, rule_code, priority, stackable, status)
SELECT 1, 1, 'COUPON', 1, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM product_promotion_rule WHERE product_id = 1 AND rule_code = 'COUPON');
