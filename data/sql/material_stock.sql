CREATE TABLE `material_stock` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `material_id` BIGINT NOT NULL COMMENT '原料ID',
  `storage_address` VARCHAR(255) NOT NULL DEFAULT '默认库位' COMMENT '库存地址',
  `available_qty` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '可用库存',
  `locked_qty` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '锁定库存',
  `total_qty` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '总库存',
  `is_del` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0否 1是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_material_storage` (`material_id`, `storage_address`),
  KEY `idx_material_id` (`material_id`),
  KEY `idx_storage_address` (`storage_address`),
  KEY `idx_is_del` (`is_del`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='原料库存表';
