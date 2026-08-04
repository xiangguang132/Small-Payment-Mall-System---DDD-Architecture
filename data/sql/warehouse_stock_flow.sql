DROP TABLE IF EXISTS `warehouse_stock_flow`;

CREATE TABLE `warehouse_stock_flow` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `quantity` DECIMAL(18,2) NOT NULL COMMENT '变动数量，入库为正，出库为负',
    `biz_type` VARCHAR(64) NOT NULL COMMENT '业务类型',
    `biz_no` VARCHAR(128) NOT NULL COMMENT '业务单号',
    `reason` VARCHAR(255) DEFAULT NULL COMMENT '原因',
    `is_del` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0否 1是',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_biz` (`biz_type`, `biz_no`),
    KEY `idx_warehouse_product` (`warehouse_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库库存流水表';