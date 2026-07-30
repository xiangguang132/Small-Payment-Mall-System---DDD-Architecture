CREATE TABLE `supplier` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '供应商ID',
  `supplier_code` VARCHAR(64) NOT NULL COMMENT '供应商编码',
  `name` VARCHAR(128) NOT NULL COMMENT '供应商名称',
  `contact_name` VARCHAR(64) DEFAULT NULL COMMENT '联系人',
  `contact_phone` VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `is_del` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0否 1是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_code` (`supplier_code`),
  KEY `idx_status_is_del` (`status`, `is_del`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';
