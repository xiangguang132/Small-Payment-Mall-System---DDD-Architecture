CREATE TABLE `material_type` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '原料类型ID',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父类型ID，0表示根类型',
  `name` VARCHAR(128) NOT NULL COMMENT '原料类型名称',
  `description` VARCHAR(512) DEFAULT NULL COMMENT '原料类型描述',
  `type_code` VARCHAR(64) DEFAULT NULL COMMENT '原料类型编码',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `is_del` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0否 1是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_code` (`type_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status_is_del` (`status`, `is_del`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='原料类型表';
