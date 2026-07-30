CREATE TABLE `supplier_material` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `supplier_id` BIGINT NOT NULL COMMENT '供应商ID',
  `material_id` BIGINT NOT NULL COMMENT '原料ID',
  `supply_price` DECIMAL(18,2) DEFAULT NULL COMMENT '供货单价',
  `lead_time_days` INT DEFAULT NULL COMMENT '供货周期（天）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `is_del` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0否 1是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_material` (`supplier_id`, `material_id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_material_id` (`material_id`),
  KEY `idx_status_is_del` (`status`, `is_del`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商原料关系表';
