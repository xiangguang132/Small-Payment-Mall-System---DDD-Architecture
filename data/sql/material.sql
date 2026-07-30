CREATE TABLE `material` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '原料ID',
  `material_code` VARCHAR(64) NOT NULL COMMENT '原料编码',
  `name` VARCHAR(128) NOT NULL COMMENT '原料名称',
  `type_id` BIGINT NOT NULL COMMENT '原料类型ID',
  `unit` VARCHAR(32) NOT NULL COMMENT '计量单位',
  `description` VARCHAR(512) DEFAULT NULL COMMENT '原料描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `is_del` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0否 1是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_material_code` (`material_code`),
  KEY `idx_type_id` (`type_id`),
  KEY `idx_status_is_del` (`status`, `is_del`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='原料表';
